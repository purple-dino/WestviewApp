//
//  CountdownLiveActivity.swift
//  Countdown
//
//  Created by E 9 on 12/11/24.
//  Copyright © 2024 orgName. All rights reserved.
//

import ActivityKit
import WidgetKit
import SwiftUI

//public struct CountdownAttributes: ActivityAttributes {
//    public struct ContentState: Codable, Hashable {
//        // Dynamic stateful properties about your activity go here!
//        
//    }
//
//    // Fixed non-changing properties about your activity go here!
//    var startTime: Date
//    var endTime: Date
//}

struct CountdownLiveActivity: Widget {
    var body: some WidgetConfiguration {
        ActivityConfiguration(for: CountdownAttributes.self) { context in
            // Lock screen/banner UI goes here
            VStack() {
                HStack {
                    VStack (){
                        Text(context.attributes.className)
                            .font(.title2)
                            .foregroundStyle(.white)
                        
                        Text("Ends at: " + formatDateToHoursAndMinutes(date: context.attributes.endTime))
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                    
                    Spacer() // This will push the two VStacks apart
                    
                    VStack (){
                        Text("Time Left: ")
                        Text(timerInterval: context.attributes.startTime...context.attributes.endTime, countsDown: true, showsHours: true)
                            .font(.title2)
                            .foregroundStyle(.white)
                    }
                    .frame(maxWidth: .infinity, alignment: .trailing)
                }

                ProgressView(timerInterval: context.attributes.startTime...context.attributes.endTime, countsDown: true, label: { Text("") }, currentValueLabel: { Text("") })
                       .progressViewStyle(LinearProgressViewStyle())
                       .frame(height: 20)
                       .scaleEffect(x: 1, y: 1.5, anchor: .center)
            }
            .padding()
            .activityBackgroundTint(.black.opacity(0.25))

        } dynamicIsland: { context in
            DynamicIsland {
                // Expanded UI goes here.  Compose the expanded UI through
                // various regions, like leading/trailing/center/bottom
                DynamicIslandExpandedRegion(.leading) {
                    Text("Leading")
                }
                DynamicIslandExpandedRegion(.trailing) {
                    Text("Trailing")
                }
                DynamicIslandExpandedRegion(.bottom) {
                    Text("Bottom")
                    // more content
                }
            } compactLeading: {
                Text("L")
            } compactTrailing: {
                Text("T")
            } minimal: {
                Text("M")
            }
            .widgetURL(URL(string: "http://www.apple.com"))
            .keylineTint(Color.red)
        }
    }
    
    func formatDateToHoursAndMinutes(date: Date) -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = "HH:mm"
        return formatter.string(from: date)
    }
}

