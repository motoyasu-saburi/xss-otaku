package main

import (
	"fmt"
	"io/ioutil"
	"net/http"
	"net/http/httputil"
	"net/url"
)

type ResponseData struct {
	header string
	body string
}

func main() {
	targetUrl := "http://example.com?p=12"

	// TODO available: POST, PUT, etc.
	req, _ := http.NewRequest("GET", targetUrl, nil)
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
	print(parseUrl(targetUrl))
}

func parseUrl(target_url string) *url.URL {
	u, err := url.Parse(target_url)
	if err != nil {
		panic(err)
	}

	// Injection Point
	for key, values := range u.Query() {
		fmt.Printf("Query Key: %s\n", key)
		for i, v := range values {
			fmt.Printf("Query Value[%d]: %s\n", i, v)
		}
	}
	return u
}

func requestTo(method, url string) *http.Response {
	req, _ := http.NewRequest(method, url, nil)
	req.Header.Set("User-Agent", "test")

	client := new(http.Client)
	resp, err := client.Do(req)
	if err != nil {
		// TODO handle error
		panic(err)
	}
	defer resp.Body.Close()

	body, err := ioutil.ReadAll(resp.Body)
    if err != nil {
        panic(err)
    }
	resp.Body
	resp.ContentLength
	resp.
	return resp
}

func isScannable(responseData *http.Response) bool {
	// TODO Analyse.
	// responseData.Body
	return true
}
