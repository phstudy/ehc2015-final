gradle dist
rm -rf Team34
unzip Team34.zip
rsync -e ssh -acv --exclude=upload.sh ./Team34/ root@team34.etu.im:./qTeam34
