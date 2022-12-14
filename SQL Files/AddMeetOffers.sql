create or alter proc AddMeetOffers (
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

