create procedure RegisterAthlete(@email varchar(50), @username nvarchar(50), @passwordSalt varchar(50), 
						   @passwordHash varchar(50), @firstName varchar(20), @lastName varchar(20), @year tinyint, 
						   @team varchar(20), @gender bit, @type varchar(8)) as
begin
	--make sure the athlete parameters aren't null
	--return 1 for everything since we don't want to give detailed info to the front end
	if(@firstName is null) begin
		raiserror('First Name cannot be null', 14, 1);
		return 1
	end
	if(@lastName is null) begin
		raiserror('Last Name cannot be null', 14, 1);
		return 1
	end
	if(@year is null or @year < 1) begin
		raiserror('Invalid year', 14, 1);
		return 1
	end
	if(@team is null) begin
		raiserror('Team cannot be null', 14, 1);
		return 1
	end
	if(@gender is null) begin
		raiserror('Gender cannot be null', 14, 1);
		return 1
	end
	if(@type is null) begin
		raiserror('Type cannot be null', 14, 1);
		return 1
	end
	if(@type <> 'Athlete') begin
		raiserror('Type must be an athlete', 14, 1);
		return 1
	end

	--get the ID of the athlete
	declare @athleteID int = (select p.ID from Person p join Athlete a on p.ID = a.ID join Team t on p.TeamID = t.ID
			   where p.FirstName = @firstName and p.LastName = @lastName and a.YearInSchool = @year 
			   and a.Gender = @gender and t.SchoolName = @team)

	--ensure the athlete actually exists
	if(@athleteID is null) begin
		raiserror('No records exist for this Athlete', 14, 1);
		return 1
	end

	--make sure the athlete doesn't already have an account
	if(exists (select * from [User] where PersonID = @athleteID)) begin
		raiserror('This athlete already has an account created', 14, 1);
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
	values (@email, @username, @passwordSalt, @passwordHash, @athleteID, @type)
	return 0
end


