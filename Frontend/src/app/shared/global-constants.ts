export class GlobalConstants{

    //Messege
    public static genericError:string = "Coś poszło nie tak. Spróbuj ponownie później";
    public static unauthorized: string = "Nie jesteś upoważniony do tej strony";

    public static productExistError:string = "Produkt nie istnieje";
    public static productAdded:string = "Produkt dodany";

    //Regex
    public static nameRegex: string = "[a-zA-Z0-9 ]*";
    public static emailRegex: string = "[A-Za-z0-9._%-]+@[A-Za-z0-9._%-]+\\.[a-z]{2,3}";
    public static contactNumberRegex: string = "^[e0-9]{9,9}$";

    //Variable
    public static error:string="error";

}