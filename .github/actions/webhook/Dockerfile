FROM alpine

RUN apk add --no-cache bash curl openssl util-linux xxd jq

COPY LICENSE /
COPY entrypoint.sh /entrypoint.sh

RUN chmod +x /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]
