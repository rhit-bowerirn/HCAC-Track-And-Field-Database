create proc DeleteCoach(
	@FName varchar(20),
	@LName varchar(20),
	@TeamName varchar(20)
)
AS 
BEGIN
	--Validate input parameter
	if(@FName is null or @LName is null)
		BEGIN
			RAISERROR('Provided First Name or Last Name cannot be null.', 14, 1);
			return 1;
		END
		
	if(@TeamName is null)
		BEGIN
			RAISERROR('Provided Team Name cannot be null.', 14, 1);
			return 2;
		END
	
	if(not exists(select * from Team where @TeamName = Team.SchoolName))
		BEGIN
			RAISERROR('Provided team name does not exist.', 14, 1);
			return 3;
		END

	declare @TeamID int
	set @TeamID = (Select ID from Team where @TeamName = Team.SchoolName)

	if(not exists(select * from Person where @FName = Person.FirstName and @LName = Person.LastName and @TeamID = Person.TeamID))
		BEGIN
			RAISERROR ('Provided person does not exist in database.', 14, 1);
			return 4;
		END

	declare @ID int
	set @ID = (select ID from Person where @FName = Person.FirstName and @LName = Person.LastName and @TeamID = Person.TeamID)

	--Check to see if the provided Coach exists
	if(not exists(select * from Coach where Coach.ID = @ID))
		BEGIN
			RAISERROR('Provided ID does not exist in Coach table.', 14, 1);
			return 5;
		END
	
	DELETE FROM Coach
	Where Coach.ID = @ID

	--Check to ensure coahc was actually deleted
	if(exists(select * from Coach where Coach.ID = @ID))
		BEGIN
			RAISERROR('Delete coach did not complete successfully.', 14, 1);
			return 6;
		END

	DELETE FROM Person
	Where Person.ID = @ID

	--Check to ensure coahc was actually deleted
	if(exists(select * from Person where Person.ID = @ID))
		BEGIN
			RAISERROR('Delete person did not complete successfully.', 14, 1);
			return 7;
		END

	--Return 0 to indicate success
	return 0;
END

--EXEC DeleteCoach @FName ='Larry',
--                 @LName = 'Cole',
--				 @TeamName = 'Rose-Hulman'