//
//  Linka.swift
//  Operator
//
//  Created by Ravil Khusainov on 08.06.2021.
//

import Foundation

extension Thing {
    struct Message: Codable {
        let linka: Linka.Command?
    }
    
    struct Linka {
        struct Command: Codable {
            let command_id: String
        }
        
        struct CommandInfo: Codable {
            let status: CommandStatus
//            let date: Date
            let mac_addr: String
            let command: String
        }
        
        enum CommandStatus: Int, Codable {
            case sent, operating, finished
        }
    }
}
