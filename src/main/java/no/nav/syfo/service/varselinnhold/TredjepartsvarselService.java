package no.nav.syfo.service.varselinnhold;

import no.nav.melding.virksomhet.servicemeldingmedkontaktinformasjon.v1.servicemeldingmedkontaktinformasjon.*;
import no.nav.syfo.domain.model.TredjepartsVarselType;
import no.nav.syfo.metric.Metrikk;
import no.nav.syfo.narmesteleder.NarmesteLederRelasjon;
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
    private Metrikk metrikk;

    @Autowired
    public TredjepartsvarselService(
            @Qualifier("tredjepartsvarselqueue") JmsTemplate tredjepartsvarselqueue,
            Metrikk metrikk

    ) {
        this.tredjepartsvarselqueue = tredjepartsvarselqueue;
        this.metrikk = metrikk;
    }

    public void sendVarselTilNaermesteLeder(TredjepartsVarselType type, NarmesteLederRelasjon narmesteLederRelasjon, List<Parameter> parametere) {
        ServicemeldingMedKontaktinformasjon melding = new ServicemeldingMedKontaktinformasjon();
        populerServiceMelding(melding, kontaktinformasjon(narmesteLederRelasjon), narmesteLederRelasjon, type, parametere);


        String xml = marshallTredjepartsServiceMelding(new ObjectFactory().createServicemelding(melding));
        tredjepartsvarselqueue.send(messageCreator(xml, randomUUID().toString()));

        metrikk.tellTredjepartVarselSendt(type.name());
    }

    private List<Kontaktinformasjon> kontaktinformasjon(NarmesteLederRelasjon narmesteLederRelasjon) {
        return asList(
                opprettKontaktinformasjon(narmesteLederRelasjon.getNarmesteLederEpost(), "EPOST"),
                opprettKontaktinformasjon(narmesteLederRelasjon.getNarmesteLederTelefonnummer(), "SMS")
        );
    }

    private void populerServiceMelding(ServicemeldingMedKontaktinformasjon servicemeldingMedKontaktinformasjon,
                                       List<Kontaktinformasjon> kontaktinformasjon,
                                       NarmesteLederRelasjon narmesteLederRelasjon,
                                       TredjepartsVarselType varseltype,
                                       List<Parameter> parametere) {
        servicemeldingMedKontaktinformasjon.setMottaker(aktoer(narmesteLederRelasjon.getNarmesteLederAktorId()));
        servicemeldingMedKontaktinformasjon.setTilhoerendeOrganisasjon(organisasjon(narmesteLederRelasjon.getOrgnummer()));
        servicemeldingMedKontaktinformasjon.setVarseltypeId(varseltype.getId());
        servicemeldingMedKontaktinformasjon.getParameterListe().addAll(parametere);
        servicemeldingMedKontaktinformasjon.getKontaktinformasjonListe().addAll(kontaktinformasjon);
    }

    private Kontaktinformasjon opprettKontaktinformasjon(String kontaktinfo, String type) {
        Kommunikasjonskanaler kanal = new Kommunikasjonskanaler();
        kanal.setValue(type);

        Kontaktinformasjon kontaktinformasjon = new Kontaktinformasjon();
        kontaktinformasjon.setKanal(kanal);
        kontaktinformasjon.setKontaktinformasjon(kontaktinfo);

        return kontaktinformasjon;
    }

    private Organisasjon organisasjon(String orgnummer) {
        Organisasjon organisasjon = new Organisasjon();
        organisasjon.setOrgnummer(orgnummer);

        return organisasjon;
    }

    private Aktoer aktoer(String aktoerId) {
        AktoerId aktoer = new AktoerId();
        aktoer.setAktoerId(aktoerId);

        return aktoer;
    }

    public static Parameter createParameter(String key, String value) {
        Parameter urlParameter = new Parameter();
        urlParameter.setKey(key);
        urlParameter.setValue(value);
        return urlParameter;
    }
}
