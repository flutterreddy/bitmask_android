package se.leap.bitmaskclient;

public interface Constants {

    //////////////////////////////////////////////
    // PREFERENCES CONSTANTS
    /////////////////////////////////////////////

    String SHARED_PREFERENCES = "LEAPPreferences";
    String PREFERENCES_APP_VERSION = "bitmask version";


    //////////////////////////////////////////////
    // EIP CONSTANTS
    /////////////////////////////////////////////

    String EIP_ACTION_CHECK_CERT_VALIDITY = "EIP.CHECK_CERT_VALIDITY";
    String EIP_ACTION_START = "EIP.START";
    String EIP_ACTION_STOP = "EIP.STOP";
    String EIP_ACTION_UPDATE = "EIP.UPDATE";
    String EIP_ACTION_IS_RUNNING = "EIP.IS_RUNNING";
    String EIP_ACTION_BLOCK_VPN_PROFILE = "EIP.ACTION_BLOCK_VPN_PROFILE";

    String EIP_NOTIFICATION = "EIP.NOTIFICATION";
    String EIP_RECEIVER = "EIP.RECEIVER";
    String EIP_REQUEST = "EIP.REQUEST";


    //////////////////////////////////////////////
    // ? CONSTANTS
    /////////////////////////////////////////////

    // TODO FIND BETTER NAMES AND DO NOT USE AS PREFERENCES KEY
    String ALLOWED_ANON = "allow_anonymous";
    String ALLOWED_REGISTERED = "allow_registration";
    String VPN_CERTIFICATE = "cert";
    String PRIVATE_KEY = "Constants.PRIVATE_KEY";
    String KEY = "Constants.KEY";
    String PROVIDER_CONFIGURED = "Constants.PROVIDER_CONFIGURED";
}
