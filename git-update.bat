@echo off
git pull
git add *
if "%1"=="" (
  git commit -m "Minor commit (surely one texture or model something like that)"
) else (
  git commit -m %1
)
git push
