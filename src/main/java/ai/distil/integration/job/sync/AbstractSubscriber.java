package ai.distil.integration.job.sync;

public abstract class AbstractSubscriber {

    public static final String HASH_CODE_FRIENDLY_NAME = "Distil Hash";

    public abstract void setHashCode(String fieldId, String hashCode);
    public abstract String getHashCode();


    //    warn impure
    public abstract void setEmail(String value);
    public abstract void setFirstName(String value);
    public abstract void setLastName(String value);
    public abstract String getEmailAddress();


}
