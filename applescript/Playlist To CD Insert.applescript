tell application "iTunes"
	copy view of front window to selectedLibrary
	copy id of selectedLibrary to playlistId
	do shell script "~/bin/make_cd_insert " & playlistId
	display alert "Done!"
end tell
