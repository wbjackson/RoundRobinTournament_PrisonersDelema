package EmployeePayrollProgram;

public class Contractors extends PersonEmployeePayroll 
{

    public Contractors() 
    {
        //super(); // calls the no-arg constructor
        setEmployedType(EmployedType.CONTRACTORS);
    }

    public float calculatePay()
    {
    	float pay = getHourlyRate() * getNumberHoursWorked();
    	setTotalEarned(pay);
    	return pay;
    }
    
    public void printType() 
    {
        System.out.println(getEmployedType());
    }
}
