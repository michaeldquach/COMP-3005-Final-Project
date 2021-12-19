public class Publisher {
    private int publisherID;
    private String publisherName;
    private String publisherEmail;
    private String publisherPhone;
    private String publisherAddress;
    private String publisherBankaccount;

    public Publisher(int publisherID, String publisherName, String publisherEmail, String publisherPhone, String publisherAddress, String publisherBankaccount){
        this.publisherID = publisherID;
        this.publisherName = publisherName;
        this.publisherEmail = publisherEmail;
        this.publisherPhone = publisherPhone;
        this.publisherAddress = publisherAddress;
        this.publisherBankaccount = publisherBankaccount;
    }

    public String toString(){
        return publisherName;
    }

    public int getPublisherID(){
        return publisherID;
    }

    public String getPublisherName(){
        return publisherName;
    }

    public String getPublisherEmail(){
        return publisherEmail;
    }

    public String getPublisherPhone(){
        return publisherPhone;
    }

    public String getPublisherAddress(){
        return publisherAddress;
    }

    public String getPublisherBankaccount(){
        return publisherBankaccount;
    }
}
