package main

import (
	"net/url"
	"testing"
)

func TestParseUrl(t *testing.T) {
	expect, _ := url.Parse("http://example.com/abcdef?param=123&def=123#asd;123")
	target := parseUrl("http://example.com/abcdef?param=123&def=123#asd;123")

	if target.Host != expect.Host {
		t.Errorf("Error!!!")
		t.Errorf("Calculate(2) = %v, expected 4", target)
	}
}
