import XCTest

import oval_apiTests

var tests = [XCTestCaseEntry]()
tests += oval_apiTests.allTests()
XCTMain(tests)
