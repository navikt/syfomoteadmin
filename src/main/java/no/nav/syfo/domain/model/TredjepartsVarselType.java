package no.nav.syfo.domain.model;

public enum TredjepartsVarselType {
    NAERMESTE_LEDER_MOTETIDSPUNKT_OPPRETTET("syfoNaermesteLederMotetidspunktOpprettet"),
    NAERMESTE_LEDER_MOTETIDSPUNKT_AVBRUTT("syfoNaermesteLederMotetidspunktAvbrutt"),
    NAERMESTE_LEDER_MOTETIDSPUNKT_AVBRUTT_BEKREFTET("syfoNaermesteLederMotetidspunktAvbruttBekreftet"),
    NAERMESTE_LEDER_MOTETIDSPUNKT_BEKREFTET("syfoNaermesteLederMotetidspunktBekreftet"),
    NAERMESTE_LEDER_MOTETIDSPUNKT_NYE_TIDSPUNKT("syfoNaermesteLederMotetidspunktNyeTidspunkt"),
    NAERMESTE_LEDER_MOTETIDSPUNKT_PAAMINNELSE("syfoNaermesteLederMotetidspunktPaaminnelse");

    private String id;

    TredjepartsVarselType(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
