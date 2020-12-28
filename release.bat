@ECHO OFF

cd /d pixeloutlaw-gradle-plugin
set releaseScope=patch
set /p releaseScope=Release scope (default - %releaseScope%)?:
..\gradlew.bat clean final publishPlugins -Prelease.scope=%releaseScope% -Pgit.root=%~dp0
