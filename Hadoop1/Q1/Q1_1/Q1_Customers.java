package Q1_1;
import  java.io.*;
import  java.util.*;


public class Q1_Customers {
    public static void main(String[] args) {
        FileOutputStream custfile = null;
	    PrintStream custfp=null;        

	try {
		File newfile = new File("Customers");
		newfile.createNewFile();
        custfile=new FileOutputStream(newfile) ;
        custfp=new PrintStream(custfile);


            for (int i=1;i<=50000;i++){
				Customers customers= new Customers();
                String string = Integer.toString(customers.id) + "," + customers.name + "," + Integer.toString(customers.age)
                         + "," + Integer.toString(customers.countryCode) + "," + Float.toString(customers.salary) + "\n";
                custfp.print(string);
                }

		 } 
		  catch(Exception e) { 
            System.out.println(e.getMessage());
           

        } 



        

        

    }
}
