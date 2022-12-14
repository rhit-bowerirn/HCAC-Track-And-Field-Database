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