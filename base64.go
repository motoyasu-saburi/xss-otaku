package main

import (
	"encoding/base64"
	"fmt"
)

func base64EncodeTest() {
	src := []byte("Hello World")

	enc := base64.StdEncoding.EncodeToString(src)

	fmt.Println(enc)

}
