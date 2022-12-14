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