CREATE or alter PROCEDURE AddMeet(@Name varchar(50), @Date datetime, @Location varchar(20)) AS
BEGIN
	IF(@Name is null OR @Name = '')
	BEGIN
		RAISERROR('Name cannot be null', 14, 1);
		RETURN 1
	END
	IF(@Date is null or ISDATE(@Date) = 0)
	BEGIN
		RAISERROR('Invalid date format. Use: MM/DD/YYYY', 14, 1);
		RETURN 2
	END
	IF(@Location is null OR @Location = '')
	BEGIN
		RAISERROR('Please provide location information', 14, 1);
		RETURN 3
	END

	IF(exists (SELECT * FROM Meet m
			   WHERE m.[Name] = @Name and m.[Location] = @Location and m.[Date] = @Date))
	BEGIN
		Print 'Meet record already exist in the DataBase';
		RETURN 0
	END

	INSERT INTO Meet ([Name], [Date], [Location])
	VALUES (@Name, @Date, @Location)
	RETURN 0
END
