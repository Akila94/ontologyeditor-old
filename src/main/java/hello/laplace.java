package hello;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
class laplace{
    public static int numberOfcolumns, numberOfrows;
    

    public static void main(String[] args){ 
        
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter x value:");
        double x = sc.nextDouble();
        System.out.println("Enter y value:");
        double y = sc.nextDouble();
        System.out.println("Enter h value:");
        double h = sc.nextDouble();
        System.out.println("Enter k value:");
        double k = sc.nextDouble();

        numberOfcolumns= (int)((x/h)+1);//i
        numberOfrows =(int) ((y/k)+1);//j
        
        System.out.println("answers:"+numberOfrows+"&"+numberOfcolumns);
        double[][] u = new double[numberOfcolumns][numberOfrows];

        
        
        //first row
        for (int i = 0; i <numberOfcolumns; i++) {
            System.out.println("value of u[" + i + ",0] ");
            u[i][0] = sc.nextDouble();
        }

        //first column
        for (int j = 1; j < numberOfrows ; j++) {
            System.out.println("value of u[0," + j + "] ");
            u[0][j] = sc.nextDouble();
        }

        //last column
        for (int j = 1; j < numberOfrows; j++) {
            System.out.println("value of u[" + (numberOfcolumns-1) + ","+j+"]");
            u[numberOfcolumns-1][j] =sc.nextDouble();
        }
        //last row
        for (int i = 1; i < numberOfcolumns-1; i++) {
            System.out.println("value of u[" + i  + ","+(numberOfrows-1)+"]");
            u[i][numberOfrows-1] = sc.nextDouble();
        }


/*

        u[0][0] = 0;
        u[0][1] = 0;
        u[0][2] = 0;
        u[0][3] = 0;
        u[0][4] = 0;
        u[1][0] = 0;
        u[2][0] = 0;
        u[3][0] = 0;
        u[4][0] = 0;
        u[4][1] = 25;
        u[4][2] = 50;
        u[4][3] = 75;
        u[4][4] = 100;
        u[1][4] = 25;
        u[2][4] = 50;
        u[3][4] = 75;

*/

        for (int j = numberOfrows-1; j >=0 ; j--) {
            for(int i=0;i<numberOfcolumns;i++){
                System.out.print(u[i][j]+"\t"); 
            }
           System.out.println(""); 
        }
        //System.out.println("up to this its ok");
        
         


        int ka = (numberOfcolumns-2)*(numberOfrows-2);
        double[][] A = new double[ka][ka];
        double[] B = new double[ka];
        int count = 0;
        double tempory= 0;
        //System.out.println("dkdks"+ ka +"k:"+k+"\n\n");
        
        //create A matrix
        for(int j=1;j<=(numberOfrows-2);j++){
            for(int i=1;i<=(numberOfcolumns-2);i++){
                if (isBoudary((i+1),j)){
                    tempory += (1/h*h)*u[i+1][j];
                }else{
                    int rowValue = i+1;
                    int columnValue = j;
                    A[count][(rowValue-1)+(numberOfcolumns-2)*(columnValue-1)] =(1/(h*h));
                }

                if (isBoudary(i,j)){
                    tempory += -2*((1/h*h)+(1/(k*k)))*u[i][j];
                }else{
                    int rowValue = i;
                    int columnValue = j;
                    A[count][(rowValue-1)+(numberOfcolumns-2)*(columnValue-1)] =-2*((1/(h*h))+(1/(k*k)));
                }

                if (isBoudary((i-1),j)){
                    tempory += (1/h*h)*u[i-1][j];
                }else{
                    int rowValue = i-1;
                    int columnValue = j;
                    A[count][(rowValue-1)+(numberOfcolumns-2)*(columnValue-1)] =(1/(h*h));
                }

                if (isBoudary(i,(j+1))){
                    tempory += (1/k*k)*u[i][(j+1)];
                }else{
                    int rowValue = i;
                    int columnValue = j+1;
                    // System.out.print("dd:"+((rowValue-1)+(numberOfcolumns-2)*(columnValue-1)));
                    A[count][(rowValue-1)+(numberOfcolumns-2)*(columnValue-1)] =(1/(k*k));
                }

                if (isBoudary(i,(j-1))){
                    tempory += (1/k*k)*u[i][(j-1)];
                }else{
                    int rowValue = i;
                    int columnValue = j-1;
                    A[count][(rowValue-1)+(numberOfcolumns-2)*(columnValue-1)] =(1/(k*k));
                }
                B[count] = tempory;    
                count++;
            }
        }

        for (int i = ka-1; i >=0 ; i--) {
            for(int j=0;j<ka;j++){
                System.out.print(A[j][i]+"\t"); 
            }
           System.out.println(""); 
        } 
        System.out.println(""); 
        
        for(int j=0;j<ka;j++){
            System.out.print(B[j]+"\t"); 
        }

    }
    
    public static boolean isBoudary(int x, int y){
        if(x==0 || y==0 || x==(numberOfcolumns-1)|| y==(numberOfrows-1)){
            return true;
        }
        else{
            return false;
        }
    }
}