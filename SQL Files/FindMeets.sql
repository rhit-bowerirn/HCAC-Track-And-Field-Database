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

--exec FindMeets
--	@name = 'test',
--	@date = '',
--	@location = ''