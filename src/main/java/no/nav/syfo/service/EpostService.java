package no.nav.syfo.service;

import no.nav.syfo.repository.dao.EpostDAO;
import no.nav.syfo.repository.model.PEpost;
import no.nav.syfo.repository.model.PEpostVedlegg;
import org.slf4j.Logger;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMultipart;
import java.io.IOException;
import java.util.List;

import static no.nav.syfo.util.OutlookEventUtil.addIcsFil;
import static no.nav.syfo.util.OutlookEventUtil.lagOutlookForesporsel;
import static no.nav.syfo.util.ToggleUtil.toggleIgnorerEpostFeil;
import static no.nav.syfo.util.ToggleUtil.toggleSendeEpost;
import static org.slf4j.LoggerFactory.getLogger;

public class EpostService {
    private static final Logger LOG = getLogger(EpostService.class);
    private static final String IKKE_SVAR_NAV_NO = "ikke-svar@nav.no";
    private static final String TEXT_HTML_CHARSET_UTF_8 = "text/html;charset=utf-8";

    @Inject
    private JavaMailSender javaMailSender;
    @Inject
    private EpostDAO epostDAO;

    public List<PEpost> finnEpostForSending() {
        return epostDAO.finnEposterForSending();
    }

    @Transactional
    public void klargjorForSending(PEpost epost) {
        if (!toggleSendeEpost()) {
            LOG.info("Sender ikke epost fordi det er togglet av!");
            return;
        }

        long epostId = epostDAO.create(epost);
        epost.vedlegg.forEach(vedlegg -> epostDAO.create(vedlegg.epostId(epostId)));
    }

    public void slettEpostEtterSending(long epostId) {
        epostDAO.delete(epostId);
    }

    public void send(PEpost epost) {
        Multipart innhold = lagOutlookForesporsel(epost.innhold);

        int vedleggNummer = 1;
        for (PEpostVedlegg epostVedlegg : epost.vedlegg) {
            innhold = addIcsFil(innhold, epostVedlegg.innhold, "Tidspunkt" + vedleggNummer++);
        }

        Multipart finalInnhold = innhold;

        try {
            String innholdTekst = hentTekstFraMimeMultipart((MimeMultipart) finalInnhold);
            MimeMessagePreparator simpleMessagePreparator = mimeMessage -> {
                MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
                messageHelper.setTo(new InternetAddress(epost.mottaker));
                messageHelper.setFrom(new InternetAddress(IKKE_SVAR_NAV_NO));
                messageHelper.setText(innholdTekst, true);
                messageHelper.setSubject(epost.emne);
            };

            javaMailSender.send(simpleMessagePreparator);
        } catch (MessagingException | IOException e) {
            LOG.error("Feil ved sending av epost", e);
            throw new RuntimeException("Feil ved sending av epost");
        } catch (Exception e) {
            if (toggleIgnorerEpostFeil()) {
                LOG.error("Feil ved sending av epost. Pga. SMTP kan finne på å sende mail selv om den gir en feil ignorerer vi den i testmiljøet." +
                        "Dette skal IKKE være på i Prod.", e);
                return;
            }
            throw e;
        }
    }

    private String hentTekstFraMimeMultipart(MimeMultipart mimeMultipart) throws MessagingException, IOException {
        String tekst = "";
        int teller = mimeMultipart.getCount();
        for (int i = 0; i < teller; i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) {
                tekst = tekst + "\n" + bodyPart.getContent();
                break; // without break same text appears twice in my tests
            } else if (bodyPart.isMimeType("text/html")) {
                String html = (String) bodyPart.getContent();
                tekst = tekst + "\n" + org.jsoup.Jsoup.parse(html).text();
            } else if (bodyPart.getContent() instanceof MimeMultipart) {
                tekst = tekst + hentTekstFraMimeMultipart((MimeMultipart) bodyPart.getContent());
            }
        }
        return tekst;
    }
}
