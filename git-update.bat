@echo off
git pull
git add *
if "%1"=="" (
  git commit -m "Added textures/models"
) else (
  git commit -m %1
)
git push
