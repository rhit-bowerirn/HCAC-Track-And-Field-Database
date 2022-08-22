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