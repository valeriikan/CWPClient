package fi.oulu.tol.esde019.cwpclient019;

import fi.oulu.tol.esde019.cwpclient019.cwprotocol.CWPControl;
import fi.oulu.tol.esde019.cwpclient019.cwprotocol.CWPMessaging;

public interface CWPProvider {
    CWPMessaging getMessaging();
    CWPControl getControl();
}
