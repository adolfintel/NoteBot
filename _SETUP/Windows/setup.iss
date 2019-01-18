[Setup]
AppId={{3E658137-CE80-49E3-8084-FD0B0158CA31}
AppName="NoteBot"
AppVersion="1.6"
AppPublisher="Federico Dossena"
AppPublisherURL="http://notebot.fdossena.com/"
AppSupportURL="http://notebot.fdossena.com/"
AppUpdatesURL="http://notebot.fdossena.com/"
DefaultDirName="{pf}\NoteBot"
DefaultGroupName="NoteBot"
DisableProgramGroupPage=yes
LicenseFile=gpl-3.0.txt
OutputDir=.
OutputBaseFilename=notebot-setup
Compression=lzma2/ultra64
LZMAAlgorithm=1
LZMAMatchFinder=BT
SolidCompression=yes
LZMANumBlockThreads=1
LZMANumFastBytes=273
LZMADictionarySize=1048576
LZMAUseSeparateProcess=yes
InternalCompressLevel=ultra64
SetupIconFile="icon.ico"
UninstallDisplayIcon="icon.ico"

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"
Name: "italian"; MessagesFile: "compiler:Languages\Italian.isl"

[Files]
Source: "setupFiles\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs sortfilesbyextension;

[Icons]
Name: "{group}\NoteBot"; Filename: "{app}\StickyNotes.exe"

[Run]
Filename:"{app}\StickyNotes.exe"; Flags:runasoriginaluser nowait;

[Registry]
Root: HKLM; Subkey: "SOFTWARE\Microsoft\Windows\CurrentVersion\Run"; ValueName: "NoteBot"; ValueType: string; ValueData: """{app}\StickyNotes.exe"" -autostartup"; Flags: uninsdeletevalue 
Root: HKLM32; Subkey: "SOFTWARE\Microsoft\Windows\CurrentVersion\Run"; ValueName: "NoteBot"; ValueType: string; ValueData: """{app}\StickyNotes.exe"" -autostartup"; Flags: uninsdeletevalue 
