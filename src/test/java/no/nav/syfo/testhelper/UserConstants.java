package no.nav.syfo.testhelper;

import no.nav.syfo.config.mocks.AktoerMock;

import static no.nav.syfo.config.mocks.OrganisasjonMock.VIRKSOMHET_NAME1;
import static no.nav.syfo.config.mocks.OrganisasjonMock.VIRKSOMHET_NAME2;

public class UserConstants {

    public static final String ARBEIDSTAKER_FNR = "12345678912";
    public static final String ARBEIDSTAKER_AKTORID = AktoerMock.mockAktorId(ARBEIDSTAKER_FNR);
    public static final String LEDER_FNR = "12987654321";
    public static final String LEDER_AKTORID = AktoerMock.mockAktorId(LEDER_FNR);
    public static final String VIRKSOMHETSNUMMER = "123456789";
    public static final String VIRKSOMHET_NAME = VIRKSOMHET_NAME1 + ", " + VIRKSOMHET_NAME2;
    public static final String NAV_ENHET = "0330";
    public static final String NAV_ENHET_NAVN = "NAV Enhet";
    public static final String VEILEDER_ID = "Z999999";
    public static final String VEILEDER_NAVN = "Veil Veileder";
    public static final String STS_TOKEN = "123456789";

    public static final String PERSON_TLF = "test@nav.no";
    public static final String PERSON_EMAIL = "12345678";
    public static final String PERSON_NAME_FIRST = "First";
    public static final String PERSON_NAME_LAST = "Last";

    public final static String PERSON_NAVN = PERSON_NAME_FIRST + " " + PERSON_NAME_LAST;
}
