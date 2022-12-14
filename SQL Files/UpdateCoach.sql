create proc UpdateCoach (
	@FName varchar(20),
	@Lname varchar(20),
	@TeamName varchar(20),
	@EventType varchar(10) = null,
	@isHeadCoach bit = null
)
AS 
BEGIN
	--Validate input parameters
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
	
	declare @originalTeamName varchar(20)

	if(@TeamName <> (select SchoolName from Team join Person p on Team.ID = p.ID 
		where @FName = p.FirstName and @Lname = p.FirstName))
		BEGIN
			RAISERROR('In not same team name branch', 14, 1);
			set @originalTeamName = (select SchoolName from Team join Person p on Team.ID = p.ID 
				where @FName = p.FirstName and @Lname = p.FirstName)
		END

	declare @TeamID int
	if(@originalTeamName is null)
		BEGIN
			if(not exists(select * from Team where @TeamName = Team.SchoolName))
				BEGIN
					RAISERROR('Provided team name does not exist.', 14, 1);
					return 2;
				END
			set @TeamID = (Select ID from Team where @TeamName = Team.SchoolName)
		END
	else 
		BEGIN
			if(not exists(select * from Team where @originalTeamName = Team.SchoolName))
				BEGIN
					RAISERROR('Provided team name does not exist.', 14, 1);
					return 2;
				END
			set @TeamID = (Select ID from Team where @originalTeamName = Team.SchoolName)
		END
	--Update inputted values

	

	--Check to see if the person exists
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

	--Update event type if inputted
	if(@EventType is not null)
		BEGIN
			--Check if given event type actually exists
			if(not exists (select * from [Event] where [Event].Type = @EventType))
				BEGIN
					RAISERROR('Provided Event Type does not exist.', 14, 1);
					return 3;
				END
			Update Coach
			SET Coach.EventType = @EventType
			WHERE Coach.ID = @ID
		END
	
	--Update isHeadCoach bit if inputted
	if(@isHeadCoach is not null)
		BEGIN
			Update Coach
			SET Coach.IsHeadCoach = @isHeadCoach
			WHERE Coach.ID = @ID
		END
	
	--Update team if team is not the same
	if(@originalTeamName is not null)
		BEGIN
			Update Person
			SET Person.TeamID = (select ID from Team where @TeamName = Team.ID)
			WHERE Person.ID = @ID
		END

	--Return 0 to indicate success
	return 0;
END


--EXEC UpdateCoach @FName = 'Larry',
--			  @LName = 'Cole',
--			  @TeamName = 'Rose-Hulman',
--			  @isHeadCoach = false
