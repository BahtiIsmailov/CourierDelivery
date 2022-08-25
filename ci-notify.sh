TIME="10"
URL="https://api.telegram.org/bot$TELEGRAM_BOT_TOKEN"

TEXT="%0A%0AProject: WBGo ver.$2%0A [Download from Gitlab]($CI_PROJECT_URL/-/jobs/$1/artifacts/download)%0ABranch:+$CI_COMMIT_REF_SLUG%0A%0A#apprelease"

msg_id=$(curl -s --max-time $TIME -d "chat_id=$TELEGRAM_USER_ID&parse_mode=markdown&text=$TEXT" $URL/sendMessage | jq '.result.message_id')

curl -v -F "chat_id=$TELEGRAM_USER_ID" -F document=@$3 $URL/sendDocument
curl -v -F "chat_id=$TELEGRAM_USER_ID" -F document=@$4 $URL/sendDocument
curl -s --max-time $TIME -d "chat_id=$TELEGRAM_USER_ID&message_id=$msg_id" $URL/pinChatMessage

