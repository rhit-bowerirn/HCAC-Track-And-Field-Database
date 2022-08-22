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
