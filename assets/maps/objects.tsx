<?xml version="1.0" encoding="UTF-8"?>
<tileset version="1.10" tiledversion="1.11.2" name="objects" tilewidth="61" tileheight="45" tilecount="8" columns="0">
 <grid orientation="orthogonal" width="1" height="1"/>
 <tile id="0" type="player">
  <properties>
   <property name="life" type="int" value="4"/>
   <property name="physics" type="class" propertytype="Physics"/>
  </properties>
  <image source="../graphics/sprites/objects/fox/idle_0.png" width="33" height="32"/>
  <objectgroup draworder="index" id="2">
   <object id="1" x="9" y="13" width="13" height="17"/>
  </objectgroup>
 </tile>
 <tile id="1" type="gem">
  <properties>
   <property name="animation_speed" type="float" value="0.6"/>
  </properties>
  <image source="../graphics/sprites/objects/gem/idle_0.png" width="15" height="13"/>
  <objectgroup draworder="index" id="2">
   <object id="1" x="2" y="2" width="11" height="9"/>
  </objectgroup>
 </tile>
 <tile id="2" type="enemy">
  <properties>
   <property name="animation_speed" type="float" value="0.75"/>
   <property name="attack" type="class" propertytype="Attack">
    <properties>
     <property name="cooldown" type="float" value="2.5"/>
     <property name="damage" type="int" value="1"/>
     <property name="range" type="float" value="1.5"/>
    </properties>
   </property>
   <property name="follow" type="class" propertytype="Follow">
    <properties>
     <property name="break_range" type="float" value="5"/>
     <property name="range" type="float" value="4"/>
    </properties>
   </property>
   <property name="life" type="int" value="1"/>
   <property name="physics" type="class" propertytype="Physics">
    <properties>
     <property name="jump_impulse" type="float" value="0"/>
     <property name="max_speed" type="float" value="1"/>
    </properties>
   </property>
   <property name="proximity_range" type="float" value="4.5"/>
   <property name="wander" type="class" propertytype="Wander">
    <properties>
     <property name="distance" type="float" value="3"/>
    </properties>
   </property>
  </properties>
  <image source="../graphics/sprites/objects/mushroom/idle_0.png" width="41" height="30"/>
  <objectgroup draworder="index" id="2">
   <object id="2" x="10" y="5" width="20" height="24"/>
  </objectgroup>
 </tile>
 <tile id="3" type="enemy">
  <properties>
   <property name="dive_peak_time" type="float" value="0.3"/>
   <property name="dive_time" type="float" value="1.2"/>
   <property name="life" type="int" value="2"/>
   <property name="proximity_range" type="float" value="9"/>
   <property name="rise_time" type="float" value="0.8"/>
  </properties>
  <image source="../graphics/sprites/objects/eagle/idle_0.png" width="40" height="41"/>
  <objectgroup draworder="index" id="2">
   <object id="1" x="5" y="2" width="28" height="35"/>
  </objectgroup>
 </tile>
 <tile id="4" type="cherry">
  <properties>
   <property name="animation_speed" type="float" value="0.8"/>
  </properties>
  <image source="../graphics/sprites/objects/cherry/idle_0.png" width="21" height="21"/>
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="4" width="15" height="13"/>
  </objectgroup>
 </tile>
 <tile id="5" type="gold-cherry">
  <image source="../graphics/sprites/objects/cherry-gold/idle_0.png" width="21" height="21"/>
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="4" width="15" height="13"/>
  </objectgroup>
 </tile>
 <tile id="6" type="enemy">
  <properties>
   <property name="animation_speed" type="float" value="0.75"/>
   <property name="attack" type="class" propertytype="Attack">
    <properties>
     <property name="cooldown" type="float" value="1"/>
     <property name="damage" type="int" value="1"/>
     <property name="range" type="float" value="4"/>
    </properties>
   </property>
   <property name="life" type="int" value="1"/>
   <property name="projectile_id" type="int" value="7"/>
   <property name="proximity_range" type="float" value="4.5"/>
  </properties>
  <image source="../graphics/sprites/objects/piranha/idle_0.png" width="61" height="45"/>
  <objectgroup draworder="index" id="2">
   <object id="1" x="20" y="16" width="21" height="29"/>
   <object id="2" name="projectile_spawn" x="37" y="25">
    <point/>
   </object>
  </objectgroup>
 </tile>
 <tile id="7">
  <properties>
   <property name="play_mode" value="loop_pingpong"/>
   <property name="speed" type="float" value="4.5"/>
  </properties>
  <image source="../graphics/sprites/objects/piranha-ball/idle_0.png" width="16" height="16"/>
  <objectgroup draworder="index" id="2">
   <object id="1" x="2" y="5" width="13" height="8"/>
  </objectgroup>
 </tile>
</tileset>
