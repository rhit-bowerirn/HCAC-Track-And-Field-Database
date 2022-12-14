-- =============================================
-- Author:		Kyle Brownell and Braedyn Edwards
-- Create date: 4/21/2022
-- Description:	Updates the team name or teams location in the Team table.
-- =============================================
-- Update date: 5/17/2022
-- Description: Changed reliance on teamID to be just teamName since they should be all unique

CREATE PROCEDURE UpdateTeam(@originalName varchar(20), @originalLocation varchar(50), @teamName varchar(20) = NULL, @location varchar(50) = NULL)
AS
BEGIN
	--makes sure the given team exists
	IF(NOT EXISTS(SELECT * FROM Team WHERE @originalName = SchoolName and @originalLocation = [Location]))
	BEGIN
		RAISERROR('Team does not exist.', 14, 1)
		RETURN 2
	END

	declare @teamID int
	set @teamID = (SELECT ID FROM Team WHERE @originalName = SchoolName and @originalLocation = [Location])

	--if given a new name, update it
	IF(@teamName IS NOT NULL and @originalName <> @teamName)
	BEGIN
		UPDATE Team
		SET SchoolName = @teamName
		WHERE ID = @teamID
	END
	--if given a new location, update it
	IF(@location IS NOT NULL and @originalLocation <> @teamName)
	BEGIN
		UPDATE Team
		SET [Location] = @location
		WHERE ID = @teamID
	END
	
	RETURN 0
END
