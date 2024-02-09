public class PaymentParams {
    private String firstName = "";
    private String lastName = "";
    private String creditCard = "";
    private String expirationDate = "";

    public void setFirstName(String f) { this.firstName = f; }
    public void setLastName(String l) { this.lastName = l; }
    public void setCreditCard(String c) { this.creditCard = c; }
    public void setExpirationDate(String e) { this.expirationDate = e; }

    //---[ Accessors ]---------------------------------------------------------
    public String getFirstName() {
        return this.firstName;
    }
    public String getLastName() {
        return this.lastName;
    }
    public String getCreditCard() {
        return this.creditCard;
    }
    public String getExpirationDate() {
        return this.expirationDate;
    }
    //---[ Accessors ]---------------------------------------------------------

    public boolean notAllFieldsFilled() {
        return firstName.isBlank() ||
                lastName.isBlank() ||
                creditCard.isBlank() ||
                expirationDate.isBlank();
    }
}
