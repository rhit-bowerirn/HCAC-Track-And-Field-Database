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

--Exec AddAthleteParticipates @FName = 'Tester', @LName = 'Test',  @YearInSchool = 2, 
--@TeamName = 'Rose-Hulman', @MeetName = 'test', @MeetDate = '2022-05-05 18:34:48:363', 
--@EventName = 'TestThrow', @score = '20 in'
