package nz.ac.canterbury.seng302.portfolio.DTO;

/**
 * A general purpose data-transferal object for STOMP messaging
 * No formatting to the content is done within this object;
 * what you put in is what comes out.
 *
 * This objects models a JSON object that looks like
 * {
 *     "content": "Whatever you want, baby!"
 * }
 */
public class MessengerSTOMP {

    private String content;

    /**
     * Constructor for our messenger
     * @param content Whatever message you want to send.
     *                Please note that there's no formatting done on this content.
     *                What you put in is what comes out.
     */
    public MessengerSTOMP(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
