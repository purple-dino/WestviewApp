//
//  CountdownAttributes.swift
//  iosApp
//
//  Created by E 9 on 12/12/24.
//  Copyright © 2024 orgName. All rights reserved.
//

import Foundation
import ActivityKit
import WidgetKit
import SwiftUI


public struct CountdownAttributes: ActivityAttributes {
    public struct ContentState: Codable, Hashable {
        // Dynamic stateful properties about your activity go here!
        
    }

    // Fixed non-changing properties about your activity go here!
    var startTime: Date
    var endTime: Date
    var className: String
}
