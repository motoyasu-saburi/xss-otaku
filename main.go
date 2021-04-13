package main

import (
	"fmt"
	"net/http"
	"net/http/httputil"
	"net/url"
)

func main() {
	url := "http://example.com?p=12"

	req, _ := http.NewRequest("GET", url, nil)
	req.Header.Set("User-Agent", "test")

	dump, _ := httputil.DumpRequestOut(req, true)
	fmt.Printf("%s", dump)

	client := new(http.Client)
	resp, err := client.Do(req)

	if err != nil {
		fmt.Println(err)
	}

	dumpResp, _ := httputil.DumpResponse(resp, true)
	fmt.Printf("%s", dumpResp)

	base64EncodeTest()
	print(parseUrl(url))
}

func parseUrl(target_url string) *url.URL {
	u, err := url.Parse(target_url)
	if err != nil {
		print(err)
	}
	// fmt.Printf("URL: %s\n", u.String())
	// fmt.Printf("Scheme: %s\n", u.Scheme)
	// fmt.Printf("Opaque: %s\n", u.Opaque)
	// fmt.Printf("User: %s\n", u.User)
	// fmt.Printf("Host: %s\n", u.Host)
	// fmt.Printf("Hostname(): %s\n", u.Hostname())
	// fmt.Printf("Path: %s\n", u.Path)
	// fmt.Printf("RawPath: %s\n", u.RawPath)
	// fmt.Printf("RawQuery: %s\n", u.RawQuery)
	// fmt.Printf("Fragment: %s\n", u.Fragment)

	for key, values := range u.Query() {
		fmt.Printf("Query Key: %s\n", key)
		for i, v := range values {
			fmt.Printf("Query Value[%d]: %s\n", i, v)
		}
	}
	return u
}
