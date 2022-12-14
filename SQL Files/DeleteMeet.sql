create or alter proc DeleteMeet(
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

--exec DeleteMeet @meetName = 'Test2', @meetTime = '05/22/2022', @meetLocation = 'Terre Haute'