package main

import (
	"net/url"
	"testing"
)

func TestParseUrl(t *testing.T) {
	expect, _ := url.Parse("example.com")
	target := parseUrl("example.com")

	if target.Host != expect.Host {
		t.Errorf("Error!!!")
		t.Errorf("Calculate(2) = %v, expected 4", target)
	}
}
