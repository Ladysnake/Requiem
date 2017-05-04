@echo off
git pull
git add *
if "%1"=="" (
  set /p msg=Commit message:
  git commit -m %msg%
) else (
  git commit -m %1
)
git push
