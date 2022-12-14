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


