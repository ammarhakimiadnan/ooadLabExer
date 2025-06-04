import java.util.Scanner;

public class MyProg{
	public static void main(String[] args){
		Student ss = new Student("Ali Baba","Male");
		System.out.print("Choose an option to continue:\n[1] Enter new subject\t\t[2] Remove subject\n[3] Show results\t\t[4] Quit\n-> ");
		Scanner input = new Scanner(System.in);
		String scode=new String("");
		Boolean cont=true;
		do{
			int choice=input.nextInt();
			switch(choice){
				case 1:
					System.out.println("Enter subject code:");
					 scode = input.next();
					System.out.println("Enter result obtained");
					char res = input.next().charAt(0);
					ss.addSubject(scode,res);
					break;
				case 2:
					System.out.println("Enter subject code to remove:");
					 scode=input.next();
					ss.remSubject(scode);
					//System.out.println(subs.get(i).code);
					break;
				case 3:
					System.out.println(ss);
					ss.printTranscript();
					break;
				case 4:
					cont=false;
					break;
				default:
					System.out.println("Unknown option");
			}
			if (cont)
		System.out.print("\nChoose an option to continue:\n[1] Enter new subject\t\t[2] Remove subject\n[3] Show results\t\t[4] Quit\n-> ");
		}while(cont==true);
	}
}
