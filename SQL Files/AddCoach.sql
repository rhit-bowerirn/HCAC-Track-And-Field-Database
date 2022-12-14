create proc AddCoach (
	@FName varchar(20),
	@LName varchar(20),
	@TeamName varchar(20),
	@EventType varchar(10),
	@isHeadCoach bit
)
AS
BEGIN
	--VALIDATE PARAMETERS
	if(@FName is null or @FName = '' or @LName is null or @LName = '')
		BEGIN
			RAISERROR('Provided First Name or Last Name cannot be null.', 14, 1)
			return 1;
		END

	if(@TeamName is null or @TeamName = '')
		BEGIN
			RAISERROR('Provided Team Name cannot be null.', 14, 1)
			return 2;
		END

	if(@EventType is null or @TeamName = '')
		BEGIN
			RAISERROR('Provided EventType cannot be null.', 14, 1)
			return 3;
		END

	if(@isHeadCoach is null or @TeamName = '')
		BEGIN
			RAISERROR('Provided isHeadCoach cannot be null.', 14, 1)
			return 4;
		END


	--Ensures Event Type is a tracked event type
	--if(not exists (select * from [Event] where [Event].Type = @EventType))
	--	BEGIN
	--		RAISERROR('Provided Event Type does not exist in Event table.', 14, 1);
	--		return 5;
	--	END
	
	--Ensures a Team actually exists in database
	if( not exists (select * from Team where Team.SchoolName = @TeamName))
		BEGIN
			RAISERROR('Provided Team Name does not exist in Team table.', 14, 1);
			return 6;
		END
	
	declare @TeamID int
	Set @TeamID = (select ID from Team where @TeamName = Team.SchoolName)

	set identity_insert Person off;
	--Check to see if person already exists in database
	if(exists (select * from Person where @FName = FirstName and @LName = LastName and @TeamID = TeamID))
		BEGIN
			RAISERROR('You cannot have duplicate people in the database.', 14, 1);
			return 7;
		END

	Insert into Person
	(FirstName, LastName, TeamID)
	values (@FName, @LName, @TeamID)

	declare @ID int
	Set @ID = IDENT_CURRENT('Person');

	--Check to see that a person was added
	if(not exists (select * from Person where Person.ID = @ID))
		BEGIN
			RAISERROR('Add Person failed.', 14, 1);
			return 8;
		END

	Insert into Coach
	(ID, EventType, IsHeadCoach)
	values (@ID, @EventType, @isHeadCoach)

	--Check to see that coach was added
	if(not exists (select * from Coach where Coach.ID = @ID))
		BEGIN
			RAISERROR('Add Coach failed.', 14, 1);
			return 9;
		END
	
	--Return 0 to indicate success
	return 0;
END




--EXEC AddCoach @FName = 'Larry',
--			  @LName = 'Cole',
--			  @TeamName = 'Rose-Hulman',
--			  @EventType = 'Running',
--			  @isHeadCoach = true