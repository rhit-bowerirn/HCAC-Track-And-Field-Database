Use S2G5Demo
go

--Add Functions

create or alter function AthleteScoreSheet(@athleteID int)
returns @scoreSheet table([Event] varchar(50), [Type] varchar(20), Score varchar(8)) as
begin
	insert into @scoreSheet ([Event], [Type], Score)
		select e.[Name], e.[Type], min(pa.Score)
		from Participates pa		
		join Athlete a on a.ID = pa.AthleteID
		left join [Event] e on pa.EventID = e.ID
		where a.ID = @athleteID and not (e.[Type] = 'Jump' or e.[Type] = 'Throw')
		group by e.[Name], e.[Type]

	insert into @scoreSheet ([Event], [Type], Score)
		select e.[Name], e.[Type], max(pa.Score)
		from Participates pa		
		join Athlete a on a.ID = pa.AthleteID
		left join [Event] e on e.ID = pa.EventID
		where a.ID = @athleteID and (e.[Type] = 'Jump' or e.[Type] = 'Throw')
		group by e.[Name], e.[Type]

	return
end

go

CREATE or alter FUNCTION ParticipatedPoints
(
	-- Add the parameters for the function here
	@meetID int,
	@athleteID int,
	@eventID int
)
RETURNS smallint
AS
BEGIN
	-- Declare the return variable here
	DECLARE @totalPoints smallint = 0,
			@eventType varchar(20),
			@gender bit
	
	SELECT @gender = a.Gender
	FROM Athlete a
	WHERE a.ID = @athleteID

	-- Add the T-SQL statements to compute the return value here
	IF(@eventType = 'Jump' OR @eventType = 'Throw')
	BEGIN
		SELECT @totalPoints = RowValues.RowNum
		FROM(
			SELECT ROW_NUMBER() OVER(ORDER BY MAX(p.Score) DESC) AS RowNum,
					p.AthleteID,
					p.EventID,
					MIN(p.MeetID) AS MeetID
			FROM Participates p
			JOIN Athlete a ON p.AthleteID = a.ID
			WHERE p.EventID = @eventID
			AND a.Gender = @gender
			GROUP BY p.AthleteID, p.EventID
			) AS RowValues
		WHERE RowValues.AthleteID = @athleteID AND
				RowValues.EventID = @eventID AND
				RowValues.MeetID = @meetID
	END
	ELSE
	BEGIN
		SELECT @totalPoints = RowValues.RowNum
		FROM(
			SELECT ROW_NUMBER() OVER(ORDER BY MIN(p.Score) ASC) AS RowNum,
					p.AthleteID,
					p.EventID ,
					MIN(p.MeetID) AS MeetID
			FROM Participates p
			JOIN Athlete a ON p.AthleteID = a.ID
			WHERE p.EventID = @eventID
			AND a.Gender = @gender
			GROUP BY p.AthleteID, p.EventID
			) AS RowValues
		WHERE RowValues.AthleteID = @athleteID AND
				RowValues.EventID = @eventID AND
				RowValues.MeetID = @meetID

	END
	IF(@totalPoints = 0)
	BEGIN
		SET @totalPoints = 0
	END
	ELSE IF(@totalPoints = 1)
	BEGIN
		SET @totalPoints = 10
	END
	ELSE IF(@totalPoints = 2)
	BEGIN
		SET @totalPoints = 8
	END
	ELSE IF(@totalPoints < 9)
	BEGIN
		SET @totalPoints = 9 - @totalPoints
	END
	ELSE
	BEGIN
		SET @totalPoints = 0
	END
	-- Return the result of the function
	RETURN @totalPoints

END

go

alter table Participates
add Points as [dbo].[ParticipatedPoints]([MeetID],[AthleteID],[EventID])


go

CREATE FUNCTION TeamTotalPoints
(
	@teamID int,
	@gender bit
)
RETURNS smallint
AS
BEGIN
	DECLARE @teamPoints smallint

	SELECT @teamPoints = SUM(p.Points)
	FROM Participates p
	JOIN Person pe ON p.AthleteID = pe.ID
	JOIN Athlete a ON p.AthleteID = a.ID
	WHERE pe.TeamID = @teamID
	AND a.Gender = @gender

	IF(@teamPoints IS NULL)
	BEGIN
		SET @teamPoints = 0
	END
	RETURN @teamPoints

END

go


--Add Computed Columns



alter table Team
add MenPoints as [dbo].[TeamTotalPoints]([ID],(1))

go

alter table Team
add WomenPoints as [dbo].[TeamTotalPoints]([ID],(0))

go


--Create SPROCS

create or alter procedure AddAthlete(@firstName varchar(20), @lastName varchar(20), @year tinyint, @team varchar(20), @gender bit) as
begin
		--Validate parameters
	if(@firstName is null) begin
		raiserror('First Name cannot be null', 14, 1);
		return 1
	end
	if(@lastName is null) begin
		raiserror('Last Name cannot be null', 14, 1);
		return 2
	end
	if(@year is null or @year < 1) begin
		raiserror('Invalid year', 14, 1);
		return 3
	end
	if(@team is null) begin
		raiserror('Team cannot be null', 14, 1);
		return 4
	end
	if(@gender is null) begin
		raiserror('Gender cannot be null', 14, 1);
		return 5;
	end

	--Check for duplicates
	if(exists (select * from Person p join Athlete a on p.ID = a.ID join Team t on p.TeamID = t.ID
			   where p.FirstName = @firstName and  p.LastName = @lastName and a.YearInSchool = @year 
			   and a.Gender = @gender and t.SchoolName = @team)) begin
		print'Athlete records already exist in the DataBase';
		return 0
	end

	--Insert into Person table
	insert into Person (FirstName, LastName, TeamID)
	values (@firstName, @lastName, (select ID from Team where SchoolName = @team))

	--Get the ID to insert
	declare @newAthleteID int = ident_current('Person')

	--Insert into Athlete table
	insert into Athlete (ID, YearInSchool, Gender)
	values (@newAthleteID, @year, @gender)

	return 0
end

go

create proc AddAthleteParticipates(
	@FName varchar(20),
	@LName varchar(20),
	@YearInSchool int,
	@gender bit,
	@TeamName varchar(20),
	@MeetName varchar(50),
	@MeetDate datetime, 
	@EventName varchar(50),
	@score varchar(8)
)
AS
BEGIN
	--VALIDATE PARAMETERS
	if(@FName is null or @FName = '') begin
		RAISERROR('ERROR: First name cannot be null.', 14, 1);
		return 1;
	end
	if(@LName is null or @LName = '') begin
		RAISERROR('ERROR: Last name cannot be null.', 14, 1);
		return 2;
	end
	if(@YearInSchool is null or @YearInSchool = '') begin
		RAISERROR('ERROR: Year In School cannot be null.', 14, 1);
		return 3;
	end
	if(@TeamName is null or @TeamName = '') begin
		RAISERROR('ERROR: Team name cannot be null.', 14, 1);
		return 4;
	end
	if(@gender is null) begin
		raiserror('ERROR: Gender cannot be null', 14, 1);
		return 5
	end
	if(@MeetName is null or @MeetName = '') begin
		RAISERROR('ERROR: Meet name cannot be null.', 14, 1);
		return 6;
	end
	if(@MeetDate is null or ISDATE(@MeetDate) = 0) begin
		RAISERROR('ERROR: Meet date cannot be null or Invalid date format. Use: MM/DD/YYYY.', 14, 1);
		return 7;
	end
	if(@EventName is null or @EventName = '') begin
		RAISERROR('ERROR: Event name cannot be null.', 14, 1);
		return 8;
	end
	if(@score is null or @score = '') begin
		RAISERROR('ERROR: Score cannot be null.', 14, 1);
		return 9;
	end

	--CHECK TO SEE IF ATHLETE EXISTS
	if(not exists(select * from Person p join Athlete a on a.id = p.id join Team t on t.id = p.TeamID
		where p.FirstName = @FName and p.LastName = @LName and a.YearInSchool = @YearInSchool 
		and a.Gender = @gender and t.SchoolName = @TeamName)) begin
		RAISERROR('ERROR: Athlete must exist in database.', 14, 1);
		return 10;
	end

	--CHECK TO SEE IF MEET EXISTS
	if(not exists(select * from Meet where [Name] = @MeetName and [Date] = @MeetDate)) begin
		RAISERROR('ERROR: Meet must exist in database.', 14, 1);
		return 11;
	end

	--CHECK TO SEE IF EVENT EXISTS
	if(not exists(select * from Event where Name = @EventName)) begin
		RAISERROR('ERROR: Event must exist in database.', 14, 1);
		return 12;
	end

	declare @meetID int
	set @meetID = (select ID from Meet where Name = @MeetName and Date = @MeetDate)
	
	declare @eventID int
	set @eventID = (select ID from Event where Name = @EventName)

	--CHECK TO SEE IF MEET OFFERS EVENT
	if(not exists(select * from Offers where meetID = @meetID and eventID = @eventID)) begin
		RAISERROR('ERROR: Meet must offer specified event.', 14, 1);
		return 13;
	end

	declare @athleteID int
	set @athleteID = (select p.ID from Person p join Athlete a on a.id = p.id join Team t on t.id = p.TeamID
		where p.FirstName = @FName and p.LastName = @LName and a.YearInSchool = @YearInSchool 
		and a.Gender = @gender and t.SchoolName = @TeamName)
	
	insert into Participates
	(AthleteID, EventID, MeetID, Score)
	values(@athleteID, @eventID, @meetID, @score)

	if(@@ERROR <> 0) begin
		RAISERROR('ERROR: Adding results failed.', 14, 1);
		return 14;
	end

	return 0;
END

go

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

go

create or alter procedure AddCompetes(@meetName varchar(50), @team varchar(20)) as
begin
	--Validate parameters
	if(@meetName is null) begin
		raiserror('Meet Name cannot be null', 14, 1);
		return 1
	end
	if(@team is null) begin
		raiserror('Team Name cannot be null', 14, 1);
		return 2
	end

	declare @meetID int = (select ID from Meet where [Name] = @meetName)
	declare @teamID int = (select ID from Team where SchoolName = @team)
	--Check for duplicates
	if(exists (select * from Competes where MeetID = @meetID and TeamID = @teamID)) begin
		print'Records already exists in the DataBase';
		return 0
	end

	--Insert into Event table
	insert into Competes (MeetID, TeamID)
	values (@meetID, @teamID)


	return 0
end

go

create or alter procedure AddEvent(@name varchar(50), @type varchar(20)) as
begin
	--Validate parameters
	if(@name is null) begin
		raiserror('Event Name cannot be null', 14, 1);
		return 1
	end
	if(@type is null) begin
		raiserror('Event Type cannot be null', 14, 1);
		return 2
	end

	--Check for duplicates
	if(exists (select * from [Event] where [Name] = @name and [Type] = @type)) begin
		print'Event already exists in the DataBase';
		return 0
	end

	--Insert into Event table
	insert into [Event] ([Name], [Type])
	values (@name, @type)


	return 0
end

go

CREATE PROCEDURE AddMeet(@Name varchar(50), @Date datetime, @Location varchar(20)) AS
BEGIN
		IF(@Name is null OR @Name = '')
	BEGIN
		RAISERROR('Name cannot be null', 14, 1);
		RETURN 1
	END
	IF(@Date is null or ISDATE(@Date) = 0)
	BEGIN
		RAISERROR('Invalid date format. Use: MM/DD/YYYY', 14, 1);
		RETURN 2
	END
	IF(@Location is null OR @Location = '')
	BEGIN
		RAISERROR('Please provide location information', 14, 1);
		RETURN 3
	END

	IF(exists (SELECT * FROM Meet m
			   WHERE m.[Name] = @Name and m.[Location] = @Location and m.[Date] = @Date))
	BEGIN
		Print 'Meet record already exist in the DataBase';
		RETURN 0
	END

	INSERT INTO Meet ([Name], [Date], [Location])
	VALUES (@Name, @Date, @Location)
	RETURN 0
END

go

create proc AddMeetOffers (
	@meetName varchar(50),
	@meetDate datetime,
	@eventName varchar(50)
)
AS
BEGIN
	--VALIDATE PARAMETERS
	if(@meetName is null or @meetName = '') begin
		RAISERROR('ERROR: Meet name cannot be null', 14, 1);
		return 1;
	end
	if(@meetDate is null or @meetDate = '' or ISDATE(@meetDate) = 0) begin
		RAISERROR('ERROR: Meet date cannot be null or Invalid date format. Use: MM/DD/YYYY', 14, 1);
		return 2
	end
	if(@eventName is null or @eventName = '') begin
		RAISERROR('ERROR: Event name cannot be null', 14, 1);
		return 3;
	end

	-- CHECK TO SEE IF MEET ACTUALLY EXISTS
	if(not exists(select * from Meet where @meetName = Name and @meetDate = Date)) begin
		RAISERROR('ERROR: Meet does not exist in database', 14, 1);
		return 4;
	end

	-- CHECK TO SEE IF EVENT ACTUALLY EXISTS
	if(not exists(select * from [Event] where @eventName = Name)) begin
		RAISERROR('ERROR: Event does not exist in database', 14, 1);
		return 5;
	end

	declare @meetID int
	set @meetID = (select ID from Meet where @meetName = Name and @meetDate = Date)

	declare @eventID int
	set @eventID = (select ID from [Event] where @eventName = Name)

	if(exists (select * from Offers where MeetID = @meetID and EventId = @eventID)) begin
		print 'Meet already offers this event';
		return 0;
	end

	insert into Offers
	values(@meetID, @eventID)

	if(@@error <> 0) begin
		RAISERROR('ERROR: An error occurred adding event to meet', 14, 1);
		return 6
	end
	
	return 0;
END

go

CREATE PROCEDURE AddTeam(@teamName varchar(20), @location varchar(50))
AS
BEGIN
	IF(@teamName IS NULL)
	BEGIN
		RAISERROR('ERROR: Team name cannot be null.', 14, 1)
		RETURN 1
	END

	IF(EXISTS(SELECT SchoolName FROM Team WHERE SchoolName = @teamName))
	BEGIN
		RAISERROR('ERROR: Team already exists with given name.', 14, 1)
		RETURN 2
	END

	IF(@location IS NULL)
	BEGIN
		RAISERROR('ERROR: Location name cannot be null.', 14, 1)
		RETURN 3
	END

	
	INSERT INTO Team (SchoolName, Location)
	VALUES(@teamName, @location)

	RETURN 0
END

go

create procedure DeleteAthlete(@firstName varchar(20), @lastName varchar(20), @year tinyint, @team varchar(20), @gender bit) as
begin
	--Validate parameters
	--Check for nulls
	if(@firstName is null) begin
		raiserror('First Name cannot be null', 14, 1);
		return 1
	end
	if(@lastName is null) begin
		raiserror('Last Name cannot be null', 14, 1);
		return 2
	end
	if(@year is null) begin
		raiserror('Year cannot be null', 14, 1);
		return 3
	end
	if(@team is null) begin
		raiserror('Team cannot be null', 14, 1);
		return 4
	end
	if(@gender is null) begin
		raiserror('Gender cannot be null', 14, 1);
		return 5
	end

	--Check that the athlete exists
	if(not exists (select * from Person p join Athlete a on p.ID = a.ID join Team t on p.TeamID = t.ID
			   where p.FirstName = @firstName and  p.LastName = @lastName and a.YearInSchool = @year 
			   and a.Gender = @gender and t.SchoolName = @team)) begin
		raiserror('There are no records for this athlete in the DataBase', 14, 1);
		return 6
	end

	--We need the athleteID for easy reference
	declare @athleteID int = (select p.ID from Person p join Athlete a on p.ID = a.ID join Team t on p.TeamID = t.ID
							  where p.FirstName = @firstName and  p.LastName = @lastName and a.YearInSchool = @year 
							  and a.Gender = @gender and t.SchoolName = @team)

	--Delete from Participates table
	delete Participates
	where AthleteID = @athleteID

	--Delete from Athlete table
	delete Athlete
	where ID = @athleteID

	--Delete from Person table
	delete Person
	where ID = @athleteID

	return 0
end

go

create proc DeleteAthleteParticipates(
	@FName varchar(20),
	@LName varchar(20),
	@YearInSchool int,
	@gender bit,
	@TeamName varchar(20),
	@MeetName varchar(50),
	@MeetDate datetime, 
	@EventName varchar(50)
)
AS
BEGIN
	--VALIDATE PARAMETERS
	if(@FName is null or @FName = '') begin
		RAISERROR('ERROR: First name cannot be null.', 14, 1);
		return 1;
	end
	if(@LName is null or @LName = '') begin
		RAISERROR('ERROR: Last name cannot be null.', 14, 1);
		return 2;
	end
	if(@YearInSchool is null or @YearInSchool = '') begin
		RAISERROR('ERROR: Year In School cannot be null.', 14, 1);
		return 3;
	end
	if(@TeamName is null or @TeamName = '') begin
		RAISERROR('ERROR: Team name cannot be null.', 14, 1);
		return 4;
	end
	if(@gender is null or @gender = '') begin
		raiserror('ERROR: Gender cannot be null', 14, 1);
		return 5
	end
	if(@MeetName is null or @MeetName = '') begin
		RAISERROR('ERROR: Meet name cannot be null.', 14, 1);
		return 6;
	end
	if(@MeetDate is null or ISDATE(@MeetDate) = 0) begin
		RAISERROR('ERROR: Meet date cannot be null or Invalid date format. Use: MM/DD/YYYY.', 14, 1);
		return 7;
	end
	if(@EventName is null or @EventName = '') begin
		RAISERROR('ERROR: Event name cannot be null.', 14, 1);
		return 8;
	end

	--CHECK TO SEE IF ATHLETE EXISTS
	if(not exists(select * from Person p join Athlete a on a.id = p.id join Team t on t.id = p.TeamID
		where p.FirstName = @FName and p.LastName = @LName and a.YearInSchool = @YearInSchool 
		and a.Gender = @gender and t.SchoolName = @TeamName)) begin
		RAISERROR('ERROR: Athlete must exist in database.', 14, 1);
		return 9;
	end

	--CHECK TO SEE IF MEET EXISTS
	if(not exists(select * from Meet where [Name] = @MeetName and [Date] = @MeetDate)) begin
		RAISERROR('ERROR: Meet must exist in database.', 14, 1);
		return 10;
	end

	--CHECK TO SEE IF EVENT EXISTS
	if(not exists(select * from Event where Name = @EventName)) begin
		RAISERROR('ERROR: Event must exist in database.', 14, 1);
		return 11;
	end

	declare @meetID int
	set @meetID = (select ID from Meet where Name = @MeetName and Date = @MeetDate)
	
	declare @eventID int
	set @eventID = (select ID from Event where Name = @EventName)

	--CHECK TO SEE IF MEET OFFERS EVENT
	if(not exists(select * from Offers where meetID = @meetID and eventID = @eventID)) begin
		RAISERROR('ERROR: Meet must offer specified event.', 14, 1);
		return 12;
	end

	declare @athleteID int
	set @athleteID = (select p.ID from Person p join Athlete a on a.id = p.id join Team t on t.id = p.TeamID
		where p.FirstName = @FName and p.LastName = @LName and a.YearInSchool = @YearInSchool 
		and a.Gender = @gender and t.SchoolName = @TeamName)
	
	delete from Participates
	where AthleteID = @athleteID and MeetID = @meetID and EventID = @eventID

	if(@@ERROR <> 0) begin
		RAISERROR('ERROR: Deleting results failed.', 14, 1);
		return 13;
	end

	return 0;
END

go

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

go

create proc DeleteMeet(
	@meetName varchar(50),
	@meetTime datetime,
	@meetLocation varchar(50)
)
AS
BEGIN
	--VALIDATE PARAMETERS
	if(@meetName is null or @meetName = '') begin
		RAISERROR('ERROR: Meet Name cannot be null', 14, 1);
		return 1;
	end
	if(@meetTime is null or @meetTime= '' or ISDATE(@meetTime) = 0) begin
		RAISERROR('ERROR: Meet Date cannot be null or Invalid date format. USE: MM/DD/YYYY', 14, 1);
		return 2;
	end
	if(@meetLocation is null or @meetLocation = '') begin
		RAISERROR('ERROR: Meet Location cannot be null', 14, 1);
		return 3;
	end

	--CHECK TO SEE IF MEET EXISTS
	if(not exists(select * from Meet where @meetName = Name and @meetTime = Date and @meetLocation = Location)) begin
		RAISERROR('ERROR: Meet must exist in database', 14, 1);
		return 4;
	END

	declare @meetID int
	set @meetID = (select ID from Meet where @meetName = Name and @meetTime = Date and @meetLocation = Location)

	--DELETE ALL RECORDS ASSOCIATED WITH MEET
	DELETE FROM Offers
	WHERE @meetID = MeetID
	
	Delete from Participates 
	where MeetID = @meetID

	--DELETE MEET
	DELETE FROM Meet
	WHERE @meetName = Name and @meetTime = Date and @meetLocation = Location

	--if(@@error <> 0) begin
		--RAISERROR('ERROR: Delete was unsucessful', 14, 1);
		--return 5;
	--end

	return 0;

END

go

create proc DeleteMeetOffers(
	@MeetName varchar(50),
	@MeetDate datetime, 
	@EventName varchar(50)
)
AS
BEGIN
	--VALIDATE PARAMETERS
	if(@MeetName is null or @MeetName = '') begin
		RAISERROR('ERROR: Meet name cannot be null.', 14, 1);
		return 1;
	end
	if(@MeetDate is null or ISDATE(@MeetDate) = 0) begin
		RAISERROR('ERROR: Meet date cannot be null or  Invalid date format. Use: MM/DD/YYYY.', 14, 1);
		return 2;
	end
	if(@EventName is null or @EventName = '') begin
		RAISERROR('ERROR: Event name cannot be null', 14, 1);
		return 3;
	end

	--CHECK TO SEE IF MEET EXISTS
	if(not exists(select * from Meet where [Name] = @MeetName and [Date] = @MeetDate)) begin
		RAISERROR('ERROR: Meet must exist in database.', 14, 1);
		return 4;
	end

	--CHECK TO SEE IF EVENT ACTUALLY EXISTS
	if(not exists(select * from [Event] where [Name] = @EventName)) begin
		RAISERROR('ERROR: Event must exist in database.', 14, 1);
		return 5;
	end

	--CHECK TO SEE IF EVENT IS OFFERED BY MEET
	declare @meetID int
	set @meetID = (select ID from Meet where [Name] = @MeetName and [Date] = @MeetDate)

	declare @eventID int
	set @eventID = (select ID from [Event] where [Name] = @EventName)

	if(not exists(select * from Offers where MeetID = @meetID and EventID = @eventID)) begin
		RAISERROR('ERROR: Meet must offer event to delete it.', 14, 1);
		return 6;
	end

	delete from Offers where @meetID = MeetID and @eventID = EventID

	if(@@ERROR <> 0) begin
		RAISERROR('Delete was unsuccessful.', 14, 1);
		return 7;
	end

	return 0;
END

go

CREATE PROCEDURE DeleteTeam(@teamName varchar(20))
AS
BEGIN
	IF(@teamName IS NULL)
	BEGIN
		RAISERROR('ERROR: Please provide a valid team name.', 14, 1)
		RETURN 1
	END

	IF(NOT EXISTS(SELECT SchoolName FROM Team WHERE SchoolName = @teamName))
	BEGIN
		RAISERROR('ERROR: Team does not exist in the database.', 14, 1)
		RETURN 2
	END

	DELETE Team
	WHERE SchoolName = @teamName

	RETURN 0
END

go

create procedure FindAthletes (@firstName varchar(20) = null, @lastName varchar(20) = null, @year tinyint = null, 
							   @team varchar(20) = null, @gender bit = null) as
begin
	--Validate parameters
	--We actually don't need to check anything, we want to be able to find lists of Athletes with similar attributes
	--We can try to be helpful if the user enters an attribute that doesn't exist however
	if(not(@firstName is null or @firstName = '' or exists (select * from Person where FirstName = @firstName))) begin
		print formatMessage('No athletes found with first name %s', @firstName);
	end
	if(not(@lastName is null or @lastName = '' or exists (select * from Person where LastName = @lastName))) begin
		print formatMessage('No athletes found with last name %s', @lastName);
	end
	if(not(@team is null or @team = '' or not(@team = 'All') or exists (select * from Person p join Team t on p.TeamID = t.ID where t.SchoolName = @team))) begin
		print formatMessage('No athletes found on team %s', @team);
	end
	if(not(@year is null or @year = '' or exists (select * from Athlete where YearInSchool = @year))) begin
		print formatMessage('No athletes found with %d years in school', @year);
	end
	--Don't need to check gender, there will definitely be data for both

	--ALL FOR ADMIN
	if(@team = 'All') begin
		select p.FirstName, p.LastName, a.YearInSchool,(case a.Gender when 1 then 'M' else 'F' end) as Gender , t.SchoolName
		from Person p 
		join Athlete a on p.ID = a.ID
		join Team t on p.TeamID = t.ID
	end
	else begin
		--Read the Athlete and Person tables
		select p.FirstName, p.LastName, a.YearInSchool, (case a.Gender when 1 then 'M' else 'F' end) as 'Gender'
		from Person p 
		join Athlete a on p.ID = a.ID
		join Team t on p.TeamID = t.ID and t.SchoolName = @team
		where @firstName is null or @firstName = '' or p.FirstName = @firstName and 
			  @lastName is null or @lastName = '' or p.LastName = @lastName and
			  @year is null or @year = '' or a.YearInSchool = @year and
			  @team is null or @team = '' or t.SchoolName = @team and
			  @gender is null or a.Gender = @gender

		return 0
	end
end

go

create proc FindAthletesMeets (
	@firstName varchar(20),
	@lastName varchar(20),
	@year tinyint,
	@gender bit,
	@teamName varchar(20)
)
AS
BEGIN

	--Validate Paramaters
	if(@firstName is null) BEGIN
		RAISERROR('Provided first name cannot be null.', 14, 1);
		return 1;
	END

	if(@lastName is null) BEGIN
		RAISERROR('Provided last name cannot be null.', 14, 1);
		return 2;
	END
	
	if(@year is null) BEGIN
		RAISERROR('Provided year in school cannot be null.', 14, 1);
		return 3;
	END	
	if(@teamName is null or not exists(select * from Team t where t.SchoolName = @teamName)) BEGIN
		RAISERROR('Provided team name cannot be null or must exist.', 14, 1);
		return 4;
	END
	if(@gender is null) begin
		raiserror('Gender cannot be null', 14, 1);
		return 5
	end
	
	declare @teamID int
	set @teamID = (select ID from Team t where t.SchoolName = @teamName);

	--Checks to see if athlete exists with the database
	if(not exists(select * from Athlete a join Person p on a.ID = p.ID 
	   where p.FirstName = @firstName and p.LastName = @lastName and a.YearInSchool = @year 
	   and a.Gender = @gender and p.TeamID = @teamID)) BEGIN
		RAISERROR('Provided athlete does not exist within the database', 14, 1);
		return 6;
    END

	declare @athleteID int
	set @athleteID = (select a.ID from Athlete a join Person p on a.ID = p.ID 
	   where p.FirstName = @firstName and p.LastName = @lastName and a.YearInSchool = @year 
	   and a.Gender = @gender and p.TeamID = @teamID)
	   

	-- Gets all of the event and meet info for the requested athlete
	select m.Name as 'Meet Name', m.Date as 'Meet Date', m.Location as 'Meet Location', e.Name as 'Event Name',
		p.Score as 'Score', p.Points as 'Points'
	from Participates p 
	join Meet m on m.ID = p.MeetID
	join [Event] e on e.ID = p.EventID
	where p.AthleteID = @athleteID

END

go

CREATE or ALTER   procedure [dbo].[FindCoach](@FName varchar(20) = null, @LName varchar(20) = null, @TeamName  varchar(20) = null) AS
BEGIN
	--Validate parameters
	--We actually don't need to check anything, we want to be able to find lists of Athletes with similar attributes
	--We can try to be helpful if the user enters an attribute that doesn't exist however
	if(not(@FName is null or @FName = '' or exists (select * from Person where FirstName = @FName))) begin
		print formatMessage('No coaches found with first name %s', @FName);
	end
	if(not(@LName is null or @LName = '' or exists (select * from Person where LastName = @LName))) begin
		print formatMessage('No coaches found with last name %s', @LName);
	end
	if(not(@TeamName is null or @TeamName = '' or not(@TeamName = 'All') or exists (select * from Person p join Team t on p.TeamID = t.ID where t.SchoolName = @TeamName))) begin
		print formatMessage('No coaches found on team %s', @TeamName);
	end
	
	if(@TeamName = 'All') begin
		select p.FirstName, p.LastName, t.SchoolName as TeamName, c.IsHeadCoach, c.EventType
		from Person p 
		join Coach c on p.ID = c.ID
		join Team t on p.TeamID = t.ID
		where @FName is null or @FName = '' or p.FirstName = @FName and 
		  @LName is null or @LName = '' or p.LastName = @LName and
		  @TeamName is null or @TeamName = '' or t.SchoolName = @TeamName
	end
	else begin
		select p.FirstName, p.LastName, t.SchoolName as TeamName, c.IsHeadCoach, c.EventType
		from Person p 
		join Coach c on p.ID = c.ID
		join Team t on p.TeamID = t.ID and t.SchoolName = @TeamName
		where @FName is null or @FName = '' or p.FirstName = @FName and 
		  @LName is null or @LName = '' or p.LastName = @LName and
		  @TeamName is null or @TeamName = '' or t.SchoolName = @TeamName
	end

	

	--Return 0 to indicate success
	return 0;	
END

go

create Proc FindCoachesTeam (
	@username nvarchar(20)
)
AS
BEGIN
	
	--Validate parameters
	if(@username is null) begin
		RAISERROR('Username cannot be null', 14, 1);
		return 1;
	end
	
	--Check to see if user exists in database
	if(not exists(select * from [User] u where u.Username = @username)) begin
		RAISERROR('User must exist in database', 14, 1);
		return 2;
	end

	declare @personID int
	set @personID = (select u.PersonID from [User] u where u.Username = @username)
	

	--Gets team name
	select t.SchoolName
	from Person p
	join Team t on t.ID = p.TeamID
	join Coach c on c.ID = p.ID
	where p.ID = @personID
	print @personID
	return 0;
END

go

create procedure FindCompetingTeams (@meetName varchar(50), @meetDate datetime, @meetLocation varchar(50)) as
begin
	--Validate parameters
	--Check for nulls
	if(@meetName is null) begin
		raiserror('Meet name cannot be null', 14, 1);
		return 1;
	end
	if(@meetDate is null) begin
		raiserror('Meet date cannot be null', 14, 1);
		return 2;
	end
	if(@meetLocation is null) begin
		raiserror('meet location cannot be null', 14, 1);
		return 3;
	end

	--Used to simplify the rest of the queries
	declare @meetID int = (select ID from Meet where [Name] = @meetName and day([Date]) = day(@meetDate) and [Location] = @meetLocation)

	--Check if the Meet exists
	if(@meetID is null) begin
		raiserror('No such meet exists', 14, 1);
		return 4;
	end
	
	--Read the Team table
	select t.SchoolName, t.[Location], t.MenPoints, t.WomenPoints
	from Team t
	join Competes c on c.TeamID = t.ID
	join Meet m on m.ID = c.MeetID
	where m.ID = @meetID

	return 0
end

go

create procedure FindEventsOffered (@meetName varchar(50), @meetDate datetime, @meetLocation varchar(50)) as
begin
	--Validate parameters
	--We actually don't need to check anything, we want to be able to find lists of Athletes with similar attributes
	--We can try to be helpful if the user enters an attribute that doesn't exist however
	if(@meetName is null) begin
		raiserror('Meet name cannot be null', 14, 1);
		return 1;
	end
	if(@meetDate is null) begin
		raiserror('Meet date cannot be null', 14, 1);
		return 2;
	end
	if(@meetLocation is null) begin
		raiserror('meet location cannot be null', 14, 1);
		return 3;
	end

	--Used to simplify the rest of the queries
	declare @meetID int = (select ID from Meet where [Name] = @meetName and day([Date]) = day(@meetDate) and [Location] = @meetLocation)

	--Check if the Meet exists
	if(@meetID is null) begin
		raiserror('No such meet exists', 14, 1);
		return 4;
	end

	--Read the Team table
	select e.[Name], e.[Type]
	from [Event] e
	join Offers o on o.EventID = e.ID
	join Meet m on m.ID = o.MeetID
	where m.ID = @meetID

	return 0
end

go

create procedure FindMeets (@name varchar(50) = null, @date datetime = null, @location varchar(50) = null) as
begin
	--Validate parameters
	--We actually don't need to check anything, we want to be able to find lists of Meets with similar attributes
	--We can try to be helpful if the user enters an attribute that doesn't exist however
	declare @errorMessage varchar
	if(not(@name is null or @name = '' or exists (select * from Meet where [Name] = @name))) begin
		print formatMessage('No Meets found named %s', @name);
	end
	if(not(@date is null or exists (select * from Meet where [Date] = @date))) begin
		print formatMessage('No meets found on %s', cast(@date as varchar));
	end
	if(not(@location is null or @location = '' or exists (select * from Meet where [Location] = @location))) begin
		print formatMessage('No meets found at %s', @location);
	end
	
	--Read the Meet table
	select [Name], [Date], [Location]
	from Meet 
	where (@name is null or @name = '' or [Name] = @name) and 
		  (@date is null or @date = '' or [Date] = @date) and
		  (@location is null or @location = '' or [Location] = @location)

	return 0
end

go

create procedure FindResults (@name varchar(50) = null, @date datetime = null, @location tinyint = null) as
begin
	--Validate parameters
	--We actually don't need to check anything, we want to be able to find lists of Athletes with similar attributes
	--We can try to be helpful if the user enters an attribute that doesn't exist however
	if(not(@name is null or @name = '' or exists (select * from Meet where [Name] = @name))) begin
		print formatMessage('No Meets found named %s', @name);
	end
	if(not(@date is null or @date = '' or exists (select * from Meet where [Date] = @date))) begin
		print formatMessage('No meets found on %s', cast(@date as varchar));
	end
	if(not(@location is null or @location = '' or exists (select * from Meet where [Location] = @location))) begin
		print formatMessage('No meets found at %s', @location);
	end
	
	--Read the Meet table
	select [Name], [Date], [Location]
	from Meet 
	where @name is null or @name = '' or [Name] = @name and 
		  @date is null or @date = '' or [Date] = @date and
		  @location is null or @location = '' or [Location] = @location

	return 0
end

go

CREATE PROCEDURE FindTeam(@teamName varchar(20) = null, @location varchar(50) = null)
AS
BEGIN
	IF(@teamName <> 'All') BEGIN
		IF(NOT (@teamName IS NULL OR EXISTS (SELECT * FROM Team WHERE SchoolName = @teamName))) BEGIN
			RAISERROR('ERROR: No teams with that name are found.', 14, 1);
			return 1;
		END
	END
	
	IF(NOT (@location IS NULL OR EXISTS (SELECT * FROM Team WHERE [Location] = @location))) BEGIN
		RAISERROR('ERROR: No teams found with specified home location', 14, 1);
		return 2;
	END
	

	--reads the team table based on parameters
	IF(@teamName = 'All') BEGIN
		SELECT *
		FROM Team 
	END
	ELSE BEGIN
		SELECT *
		FROM Team 
		WHERE @teamName IS NULL OR  SchoolName = @teamName AND 
			  @location IS NULL OR [Location] = @location
	END

	RETURN 0
END

go

create Proc FindUserType(
	@username nvarchar(50)
)
AS
BEGIN
	if(@username is null) begin
		RAISERROR('Username cannot be null', 14, 1);
		return 1;
	end

	if(not exists(select * from [User] where [User].username = @username)) begin
		RAISERROR('User must exist in database', 14, 1);
		return 1;
	end

	select [Type]
	from [User]
	where username = @username;

END

go

create procedure [dbo].[ListEventResults] (@meetName varchar(50), @meetDate datetime, @meetLocation varchar(50), @eventName varchar(50)) as
begin
	--Validate parameters
	--Check for nulls
	if(@meetName is null) begin
		raiserror('Meet name cannot be null', 14, 1);
		return 1;
	end
	if(@meetDate is null) begin
		raiserror('Meet date cannot be null', 14, 1);
		return 2;
	end
	if(@meetLocation is null) begin
		raiserror('Meet location cannot be null', 14, 1);
		return 3;
	end
	if(@eventName is null) begin
		raiserror('Event name cannot be null', 14, 1);
		return 4;
	end
	
	--Used to simplify the rest of the queries
	declare @meetID int = (select ID from Meet where [Name] = @meetName and day([Date]) = day(@meetDate) and [Location] = @meetLocation)
	declare @eventID int = (select ID from [Event] where [Name] = @eventName)
	declare @eventType varchar = (select [Type] from Event where [Name] = @eventName)

	--Check that the Meet exists
	if(@meetID is null) begin
		raiserror('No such meet exists', 14, 1);
		return 5;
	end
	--Check that the Event exists
	if(@eventID is null) begin
		raiserror('No such event exists', 14, 1);
		return 6;
	end
	if(not exists (select * from Offers where MeetID = @meetID and EventID = @eventID)) begin
		declare @errorMessage varchar = formatMessage('%s does not offer %s', @meetName, @eventName)
		raiserror(@errorMessage, 14, 1);
		return 7;
	end

	--Read Person, Athlete, Team and Participates for Event results
	select p.FirstName, p.LastName, a.YearInSchool, (case a.Gender when 1 then 'M' else 'F' end) as 'Gender', t.SchoolName, pa.Score, pa.Points
	from Participates pa
	join Meet m on pa.MeetID = m.ID
	join [Event] e on pa.EventID = e.ID
	join Athlete a on pa.AthleteID = a.ID
	join Person p on a.ID = p.ID
	join Team t on p.TeamID = t.ID
	where pa.MeetID = @meetID and pa.EventID = @eventID
	order by pa.Points desc

	return 0
end

go

create procedure ListPBsByAthlete(@firstName varchar(20), @lastName varchar(20), @year tinyint, @team varchar(20), @gender bit) as
begin
	--Validate parameters
	--Check for nulls
	if(@firstName is null) begin
		raiserror('First Name cannot be null', 14, 1);
		return 1
	end
	if(@lastName is null) begin
		raiserror('Last Name cannot be null', 14, 1);
		return 2
	end
	if(@year is null or @year < 1) begin
		raiserror('Invalid year', 14, 1);
		return 3
	end
	if(@team is null) begin
		raiserror('Team cannot be null', 14, 1);
		return 4
	end
	if(@gender is null) begin
		raiserror('Gender cannot be null', 14, 1);
		return 5
	end

	if(not exists (select * from Person p join Athlete a on p.ID = a.ID join Team t on p.TeamID = t.ID
		where p.FirstName = @firstName and  p.LastName = @lastName and a.YearInSchool = @year 
		and a.Gender = @gender and t.SchoolName = @team)) begin
		raiserror('No records found for this Athlete', 14, 1);
		return 6
	end
	
	--Used to simplify the rest of the queries
	declare @athleteID int = (select a.ID from Athlete a join Person p on a.ID = p.ID join Team t on p.TeamID = t.ID 
							  where p.FirstName = @firstName and p.LastName = @lastName and a.YearInSchool = @year 
							  and a.Gender = @gender and t.SchoolName = @team)

	select * from AthleteScoreSheet(@athleteID);

end

go

create procedure ListPBsByEvent (@eventName varchar(50)) as
begin
	--Validate parameters
	--Check for nulls

	if(@eventName is null) begin
		raiserror('Event name cannot be null', 14, 1);
		return 1;
	end

	declare @eventID int = (select ID from [Event] where [Name] = @eventName)
	declare @eventType varchar(20) = (select [Type] from [Event] where [Name] = @eventName)

	--Check that the Event exists
	if(@eventID is null) begin
		raiserror('No such event exists', 14, 1);
		return 2;
	end

	if(@eventType = 'Jump' or @eventType = 'Throw') begin
		select p.FirstName, p.LastName, a.YearInSchool, (case a.Gender when 1 then 'M' else 'F' end) as 'Gender', t.SchoolName, max(pa.Score) as 'Mark'
		from Participates pa
		join [Event] e on pa.EventID = e.ID
		join Athlete a on a.ID = pa.AthleteID
		join Person p on p.ID = a.ID
		join Team t on t.ID = p.TeamID
		where e.ID = @eventID
		group by p.FirstName, p.LastName, a.YearInSchool, t.SchoolName, a.Gender
		order by max(pa.Score) desc
	end
	else begin
		select p.FirstName, p.LastName, a.YearInSchool, (case a.Gender when 1 then 'M' else 'F' end) as 'Gender', t.SchoolName, min(pa.Score) as 'Time'
		from Participates pa
		join [Event] e on pa.EventID = e.ID
		join Athlete a on a.ID = pa.AthleteID
		join Person p on p.ID = a.ID
		join Team t on t.ID = p.TeamID
		where e.ID = @eventID
		group by p.FirstName, p.LastName, a.YearInSchool, t.SchoolName, a.Gender
		order by min(pa.Score) asc
	end

	return 0
end

go

create procedure RegisterAthlete(@email varchar(50), @username nvarchar(50), @passwordSalt varchar(50), 
						   @passwordHash varchar(50), @firstName varchar(20), @lastName varchar(20), @year tinyint, 
						   @team varchar(20), @gender bit, @type varchar(8)) as
begin
	--make sure the athlete parameters aren't null
	--return 1 for everything since we don't want to give detailed info to the front end
	if(@firstName is null) begin
		raiserror('First Name cannot be null', 14, 1);
		return 1
	end
	if(@lastName is null) begin
		raiserror('Last Name cannot be null', 14, 1);
		return 1
	end
	if(@year is null or @year < 1) begin
		raiserror('Invalid year', 14, 1);
		return 1
	end
	if(@team is null) begin
		raiserror('Team cannot be null', 14, 1);
		return 1
	end
	if(@gender is null) begin
		raiserror('Gender cannot be null', 14, 1);
		return 1
	end
	if(@type is null) begin
		raiserror('Type cannot be null', 14, 1);
		return 1
	end
	if(@type <> 'Athlete') begin
		raiserror('Type must be an athlete', 14, 1);
		return 1
	end

	--get the ID of the athlete
	declare @athleteID int = (select p.ID from Person p join Athlete a on p.ID = a.ID join Team t on p.TeamID = t.ID
			   where p.FirstName = @firstName and p.LastName = @lastName and a.YearInSchool = @year 
			   and a.Gender = @gender and t.SchoolName = @team)

	--ensure the athlete actually exists
	if(@athleteID is null) begin
		raiserror('No records exist for this Athlete', 14, 1);
		return 1
	end

	--make sure the athlete doesn't already have an account
	if(exists (select * from [User] where PersonID = @athleteID)) begin
		raiserror('This athlete already has an account created', 14, 1);
		return 1
	end
	
	--make sure the email and username aren't taken
	if(exists (select * from [User] where Email = @email)) begin
		raiserror('An account already exists with that email', 14, 1);
		return 1
	end
	if(exists (select * from [User] where Username = @username)) begin
		raiserror('An account already exists with that username', 14, 1);
		return 1
	end
	

	--make sure registration info isn't null
	if(@email is null) begin
		raiserror('email cannot be null', 14, 1);
		return 1
	end
	if(@username is null) begin
		raiserror('Username cannot be null', 14, 1);
		return 1
	end
	if(@passwordSalt is null) begin
		raiserror('Password Salt cannot be null', 14, 1);
		return 1
	end
	if(@passwordHash is null) begin
		raiserror('Password Hash cannot be null', 14, 1);
		return 1
	end
	
	--Insert into User table 
	insert into [User] (Email, Username, PasswordSalt, PasswordHash, PersonID, [Type])
	values (@email, @username, @passwordSalt, @passwordHash, @athleteID, @type)
	return 0
end

go

create procedure RegisterCoach(@email varchar(50), @username nvarchar(50), @passwordSalt varchar(50), @passwordHash varchar(50), 
						@firstName varchar(20), @lastName varchar(20), @team varchar(20), @eventType varchar(10), 
						@isHeadCoach bit, @type varchar(8)) as
begin
	--make sure the coach's parameters aren't null
	if(@firstName is null) begin
		raiserror('First Name cannot be null', 14, 1);
		return 1
	end
	if(@lastName is null) begin
		raiserror('Last Name cannot be null', 14, 1);
		return 1
	end
	if(@team is null) begin
		raiserror('Team cannot be null', 14, 1);
		return 1
	end
	if(@eventType is null) begin
		raiserror('eventType cannot be null', 14, 1);
		return 1
	end
	if(@isHeadCoach is null) begin
		raiserror('isHeadCoach cannot be null', 14, 1);
		return 1
	end
	if(@type is null) begin
		raiserror('Type cannot be null', 14, 1);
		return 1
	end
	if(@type <> 'Coach') begin
		raiserror('Type must be an coach', 14, 1);
		return 1
	end


	--get the ID of the coach
	declare @coachID int = (select p.ID from Person p join Coach c on p.ID = c.ID join Team t on p.TeamID = t.ID
						    where p.FirstName = @firstName and p.LastName = @lastName and c.eventType = @eventType 
							and c.isHeadCoach = @isHeadCoach and t.SchoolName = @team)

	--ensure the coach actually exists
	if(@coachID is null) begin
		raiserror('No records exist for this Coach', 14, 1);
		return 1
	end

	--make sure the coach doesn't already have an account
	if(exists (select * from [User] where PersonID = @coachID)) begin
		raiserror('This coach already has an account created', 14, 1);
		return 1
	end
	
	--make sure the email and username aren't taken
	if(exists (select * from [User] where Email = @email)) begin
		raiserror('An account already exists with that email', 14, 1);
		return 1
	end
	if(exists (select * from [User] where Username = @username)) begin
		raiserror('An account already exists with that username', 14, 1);
		return 1
	end
	

	--make sure registration info isn't null
	if(@email is null) begin
		raiserror('email cannot be null', 14, 1);
		return 1
	end
	if(@username is null) begin
		raiserror('Username cannot be null', 14, 1);
		return 1
	end
	if(@passwordSalt is null) begin
		raiserror('Password Salt cannot be null', 14, 1);
		return 1
	end
	if(@passwordHash is null) begin
		raiserror('Password Hash cannot be null', 14, 1);
		return 1
	end
	
	--Insert into User table 
	insert into [User] (Email, Username, PasswordSalt, PasswordHash, PersonID, [Type])
	values (@email, @username, @passwordSalt, @passwordHash, @coachID, @type)
	return 0
end

go

create procedure UpdateAthlete(@originalFirstName varchar(20), @firstName varchar(20) = null, @originalLastName varchar(20), 
							   @lastName varchar(20) = null, @originalYear tinyint, @year tinyint = null, 
							   @originalTeam varchar(20), @team varchar(20) = null, @originalGender bit, 
							   @gender bit = null) as
begin
	--Validate parameters
	if(@firstName is null) begin
		raiserror('First Name cannot be null', 14, 1);
		return 1
	end
	if(@lastName is null) begin
		raiserror('Last Name cannot be null', 14, 1);
		return 2
	end
	if(@year is null or @year < 1) begin
		raiserror('Invalid year', 14, 1);
		return 3
	end
	if(@team is null) begin
		raiserror('Team cannot be null', 14, 1);
		return 4
	end
	if(@gender is null) begin
		raiserror('Gender cannot be null', 14, 1);
		return 5
	end


	declare @teamID varchar(20)
	if(@team is not null) begin
		set @teamID = (select ID from Team where SchoolName = @team)
	end

	--get the ID of the athlete
	declare @athleteID int = (select p.ID from Person p join Athlete a on p.ID = a.ID join Team t on p.TeamID = t.ID
			   where p.FirstName = @originalFirstName and p.LastName = @originalLastName and a.YearInSchool = @originalYear 
			   and a.Gender = @originalGender and t.SchoolName = @originalTeam)

	--ensure the athlete actually exists
	if(@athleteID is null) begin
		raiserror('No records exist for this Athlete', 14, 1);
		return 6
	end

	--Update Person table
	update Person
	set FirstName = isNull(@firstName, FirstName), LastName = isNull(@lastName, LastName), TeamID =	isNull(@teamID, TeamID)
	where ID = @athleteID

	--Update Athlete Table
	update Athlete
	set YearInSchool = isNull(@year, YearInSchool), Gender = isNull(@gender, Gender)
	where ID = @athleteID

	return 0
end

go

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

go

CREATE PROCEDURE UpdateTeam(@originalName varchar(20), @originalLocation varchar(50), @teamName varchar(20) = NULL, @location varchar(50) = NULL)
AS
BEGIN
	--makes sure the given team exists
	IF(NOT EXISTS(SELECT * FROM Team WHERE @originalName = SchoolName and @originalLocation = [Location]))
	BEGIN
		RAISERROR('Team does not exist.', 14, 1)
		RETURN 2
	END

	declare @teamID int
	set @teamID = (SELECT ID FROM Team WHERE @originalName = SchoolName and @originalLocation = [Location])

	--if given a new name, update it
	IF(@teamName IS NOT NULL and @originalName <> @teamName)
	BEGIN
		UPDATE Team
		SET SchoolName = @teamName
		WHERE ID = @teamID
	END
	--if given a new location, update it
	IF(@location IS NOT NULL and @originalLocation <> @teamName)
	BEGIN
		UPDATE Team
		SET [Location] = @location
		WHERE ID = @teamID
	END
	
	RETURN 0
END

go

create PROCEDURE ListTeamRankings
	-- Add the parameters for the stored procedure here
	@gender bit
AS
BEGIN
	IF(@gender is null)
	BEGIN
		RAISERROR('Gender cannot be null.',14,1)
		RETURN 1
	END
	IF(@gender = 1)
	BEGIN
		SELECT ROW_NUMBER() OVER(ORDER BY (t.MenPoints) DESC) AS Ranking, t.SchoolName, t.MenPoints AS Points
		FROM Team t
		ORDER BY t.MenPoints DESC
	END
	ELSE
	BEGIN

		SELECT ROW_NUMBER() OVER(ORDER BY (t.WomenPoints) DESC) AS Ranking, t.SchoolName, t.WomenPoints AS Points
		FROM Team t
		ORDER BY t.WomenPoints DESC
	END
	RETURN 0
END