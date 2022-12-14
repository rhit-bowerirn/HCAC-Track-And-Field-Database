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