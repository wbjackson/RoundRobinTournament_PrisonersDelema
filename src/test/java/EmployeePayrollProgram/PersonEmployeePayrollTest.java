package EmployeePayrollProgram;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class PersonEmployeePayrollTest 
{

    private static final float EPS = 0.0001f; // small tolerance for float comparisons

    @Test   
    void contractorCalculatePay() 
    {
        PersonEmployeePayroll c = new Contractors();
        c.setHourlyRate(50f);
        c.setNumberHoursWorked(45);

        float pay = c.calculatePay();

        assertEquals(PersonEmployeePayroll.EmployedType.CONTRACTORS, c.getEmployedType()); // checks correct employee type is set
        assertEquals(50f * 45f, pay, EPS); // verifies contractor pay = rate × hours
        assertEquals(pay, c.getTotalEarned(), EPS); // ensures totalEarned was updated with calculated pay
    }

    @Test 
    void employeeCalculatePayNoOvertime() 
    {
        PersonEmployeePayroll e = new Employee();
        e.setHourlyRate(20f);
        e.setNumberHoursWorked(40);

        float pay = e.calculatePay();

        assertEquals(PersonEmployeePayroll.EmployedType.EMPLOYEES, e.getEmployedType()); // checks correct employee type
        assertEquals(20f * 40f, pay, EPS); // verifies normal pay with no overtime

        assertEquals(0f, e.getTotalEarned(), EPS); // confirms Employee class does NOT update totalEarned
    }

    @Test
    void employeeCalculatePayWithOvertime() 
    {
        PersonEmployeePayroll e = new Employee();
        e.setHourlyRate(20f);
        e.setNumberHoursWorked(45);

        float pay = e.calculatePay();
        float expected = (40f * 20f) + (5f * 20f * 1.5f);

        assertEquals(PersonEmployeePayroll.EmployedType.EMPLOYEES, e.getEmployedType()); // checks employee type
        assertEquals(expected, pay, EPS); // verifies overtime formula is applied correctly

        assertEquals(0f, e.getTotalEarned(), EPS); // confirms totalEarned is still not updated for Employee
    }

    @Test
    void salaryEmployeeCalculatePayAlways40Hours() 
    {
        PersonEmployeePayroll s = new SalaryEmployee();
        s.setHourlyRate(30f);
        s.setNumberHoursWorked(10); 

        float pay = s.calculatePay();

        assertEquals(PersonEmployeePayroll.EmployedType.SALARYEMPLOYEES, s.getEmployedType()); // checks salary employee type
        assertEquals(40f * 30f, pay, EPS); // verifies salary pay always uses 40 hours
        assertEquals(pay, s.getTotalEarned(), EPS); // ensures totalEarned is updated correctly
    }

    @Test
    void polymorphismWorksInArray() 
    {
        PersonEmployeePayroll[] workers = 
        	{
                new Contractors(),
                new Employee(),
                new SalaryEmployee()
        };

        workers[0].setHourlyRate(50f);
        workers[0].setNumberHoursWorked(45);

        workers[1].setHourlyRate(20f);
        workers[1].setNumberHoursWorked(45);

        workers[2].setHourlyRate(30f);
        workers[2].setNumberHoursWorked(100); // ignored for salary

        float pay0 = workers[0].calculatePay(); // contractor
        float pay1 = workers[1].calculatePay(); // employee OT
        float pay2 = workers[2].calculatePay(); // salary 40 hrs

        assertEquals(2250f, pay0, EPS); // verifies contractor pay calculation
        assertEquals(950f, pay1, EPS);  // verifies employee overtime calculation
        assertEquals(1200f, pay2, EPS); // verifies salary employee fixed 40-hour pay

        assertEquals(PersonEmployeePayroll.EmployedType.CONTRACTORS, workers[0].getEmployedType()); // confirms contractor type 
        assertEquals(PersonEmployeePayroll.EmployedType.EMPLOYEES, workers[1].getEmployedType());   // confirms employee type 
        assertEquals(PersonEmployeePayroll.EmployedType.SALARYEMPLOYEES, workers[2].getEmployedType()); // confirms salary employee type 
    }
}
