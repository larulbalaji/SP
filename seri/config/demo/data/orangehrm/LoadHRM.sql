-- First, the category data. Location, Job Titles etc.
source Location.sql
source JobTitle.sql
source Departments.sql
source EmploymentStatus.sql
-- Now the Employee Data
source Employees.sql
source EmpManager.sql
source EmpJobtitle.sql
source EmpLocation.sql
source EmpDepartment.sql
source EmpEmpStatus.sql
-- Now the Contractors
source Contractors.sql
source ConManager.sql
source ConJobtitle.sql
source ConLocation.sql
source ConDepartment.sql
source ConEmpStatus.sql
-- Now update the Unique IDs table
-- This maintains the last number used for things such as Employee Number
-- These are updated by the app, rather than database 'insert' statements
-- So we need to set them here
source UpdateUniqueIDs.sql

