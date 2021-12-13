package no.nav.syfo.service.varselinnhold;

import no.nav.melding.virksomhet.servicemeldingmedkontaktinformasjon.v1.servicemeldingmedkontaktinformasjon.*;
import no.nav.syfo.consumer.narmesteleder.NarmesteLederRelasjonDTO;
import no.nav.syfo.consumer.pdl.PdlConsumer;
import no.nav.syfo.domain.AktorId;
import no.nav.syfo.domain.Fodselsnummer;
import no.nav.syfo.domain.model.TredjepartsVarselType;
import no.nav.syfo.metric.Metric;
import org.springframework.beans.factory.annotation.*;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static no.nav.syfo.util.JAXB.marshallTredjepartsServiceMelding;
import static no.nav.syfo.util.JmsUtil.messageCreator;

@Service
@Transactional
public class TredjepartsvarselService {

    @Value("${tjenester.url}")
    private String tjenesterUrl;

    private JmsTemplate tredjepartsvarselqueue;
    private Metric metric;
    private PdlConsumer pdlConsumer;

    @Autowired
    public TredjepartsvarselService(
            @Qualifier("tredjepartsvarselqueue") JmsTemplate tredjepartsvarselqueue,
            Metric metric,
            PdlConsumer pdlConsumer

    ) {
        this.tredjepartsvarselqueue = tredjepartsvarselqueue;
        this.metric = metric;
        this.pdlConsumer = pdlConsumer;
    }

    public void sendVarselTilNaermesteLeder(TredjepartsVarselType type, NarmesteLederRelasjonDTO narmesteLederRelasjon, List<WSParameter> parametere) {
        WSServicemeldingMedKontaktinformasjon melding = new WSServicemeldingMedKontaktinformasjon();
        populerServiceMelding(melding, kontaktinformasjon(narmesteLederRelasjon), narmesteLederRelasjon, type, parametere);


        String xml = marshallTredjepartsServiceMelding(new ObjectFactory().createServicemelding(melding));
        tredjepartsvarselqueue.send(messageCreator(xml, randomUUID().toString()));

        metric.tellTredjepartVarselSendt(type.name());
    }

    private List<WSKontaktinformasjon> kontaktinformasjon(NarmesteLederRelasjonDTO narmesteLederRelasjon) {
        return asList(
                opprettKontaktinformasjon(narmesteLederRelasjon.getNarmesteLederEpost(), "EPOST"),
                opprettKontaktinformasjon(narmesteLederRelasjon.getNarmesteLederTelefonnummer(), "SMS")
        );
    }

    private void populerServiceMelding(WSServicemeldingMedKontaktinformasjon servicemeldingMedKontaktinformasjon,
                                       List<WSKontaktinformasjon> kontaktinformasjon,
                                       NarmesteLederRelasjonDTO narmesteLederRelasjon,
                                       TredjepartsVarselType varseltype,
                                       List<WSParameter> parametere) {
        AktorId arbeidsgiverAktorId = pdlConsumer.aktorId(new Fodselsnummer(narmesteLederRelasjon.getNarmesteLederPersonIdentNumber()));

        servicemeldingMedKontaktinformasjon.setMottaker(aktoer(arbeidsgiverAktorId.getValue()));
        servicemeldingMedKontaktinformasjon.setTilhoerendeOrganisasjon(organisasjon(narmesteLederRelasjon.getVirksomhetsnummer()));
        servicemeldingMedKontaktinformasjon.setVarseltypeId(varseltype.getId());
        servicemeldingMedKontaktinformasjon.getParameterListe().addAll(parametere);
        servicemeldingMedKontaktinformasjon.getKontaktinformasjonListe().addAll(kontaktinformasjon);
    }

    private WSKontaktinformasjon opprettKontaktinformasjon(String kontaktinfo, String type) {
        WSKommunikasjonskanaler kanal = new WSKommunikasjonskanaler();
        kanal.setValue(type);

        WSKontaktinformasjon kontaktinformasjon = new WSKontaktinformasjon();
        kontaktinformasjon.setKanal(kanal);
        kontaktinformasjon.setKontaktinformasjon(kontaktinfo);

        return kontaktinformasjon;
    }

    private WSOrganisasjon organisasjon(String orgnummer) {
        WSOrganisasjon organisasjon = new WSOrganisasjon();
        organisasjon.setOrgnummer(orgnummer);

        return organisasjon;
    }

    private WSAktoer aktoer(String aktoerId) {
        WSAktoerId aktoer = new WSAktoerId();
        aktoer.setAktoerId(aktoerId);

        return aktoer;
    }

    public static WSParameter createParameter(String key, String value) {
        WSParameter urlParameter = new WSParameter();
        urlParameter.setKey(key);
        urlParameter.setValue(value);
        return urlParameter;
    }
}
