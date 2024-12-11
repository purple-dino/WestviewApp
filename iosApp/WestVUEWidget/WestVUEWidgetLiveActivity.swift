//
//  WestVUEWidgetLiveActivity.swift
//  WestVUEWidget
//
//  Created by E 9 on 10/9/24.
//  Copyright © 2024 orgName. All rights reserved.
//

import ActivityKit
import WidgetKit
import SwiftUI

struct WestVUEWidgetAttributes: ActivityAttributes {
    public struct ContentState: Codable, Hashable {
        // Dynamic stateful properties about your activity go here!
        var emoji: String
    }

    // Fixed non-changing properties about your activity go here!
    var name: String
}

struct WestVUEWidgetLiveActivity: Widget {
    var body: some WidgetConfiguration {
        ActivityConfiguration(for: WestVUEWidgetAttributes.self) { context in
            // Lock screen/banner UI goes here
            VStack {
                Text("Hello \(context.state.emoji)")
            }
            .activityBackgroundTint(Color.cyan)
            .activitySystemActionForegroundColor(Color.black)

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
                    Text("Bottom \(context.state.emoji)")
                    // more content
                }
            } compactLeading: {
                Text("L")
            } compactTrailing: {
                Text("T \(context.state.emoji)")
            } minimal: {
                Text(context.state.emoji)
            }
            .widgetURL(URL(string: "http://www.apple.com"))
            .keylineTint(Color.red)
        }
    }
}

extension WestVUEWidgetAttributes {
    fileprivate static var preview: WestVUEWidgetAttributes {
        WestVUEWidgetAttributes(name: "World")
    }
}

extension WestVUEWidgetAttributes.ContentState {
    fileprivate static var smiley: WestVUEWidgetAttributes.ContentState {
        WestVUEWidgetAttributes.ContentState(emoji: "😀")
     }
     
     fileprivate static var starEyes: WestVUEWidgetAttributes.ContentState {
         WestVUEWidgetAttributes.ContentState(emoji: "🤩")
     }
}

#Preview("Notification", as: .content, using: WestVUEWidgetAttributes.preview) {
   WestVUEWidgetLiveActivity()
} contentStates: {
    WestVUEWidgetAttributes.ContentState.smiley
    WestVUEWidgetAttributes.ContentState.starEyes
}
